package com.carddemo.batch.posting;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.model.entity.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: CBTRN02C — daily transaction posting, run by JCL POSTTRAN (rejects land in the
 * DALYREJS GDG created by DALYREJS.jcl).
 *
 * <p>Files and copybooks: DALYTRAN (CVTRA06Y), CARDXREF (CVACT03Y), ACCTDATA (CVACT01Y),
 * TCATBALF (CVTRA01Y), TRANSACT (CVTRA05Y) and the inline REJECT-RECORD layout of CBTRN02C.
 *
 * <p>Paragraph mapping: {@code 1500-VALIDATE-TRAN} / {@code 1500-A-LOOKUP-XREF} /
 * {@code 1500-B-LOOKUP-ACCT} → {@link #validate(DailyTransaction)}; {@code 2000-POST-TRANSACTION}
 * with {@code 2700-UPDATE-TCATBAL}, {@code 2800-UPDATE-ACCOUNT-REC} and
 * {@code 2900-WRITE-TRANSACTION-FILE} → {@link #post(DailyTransaction)}, executed in that order.
 */
@Service
public class TransactionPostingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingService.class);

    /** Z-GET-DB2-FORMAT-TIMESTAMP: EEEE-MM-DD-UU.MM.SS.HH0000 (PIC X(26)). */
    private static final DateTimeFormatter DB2_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS");
    private static final String DB2_REST = "0000";

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository categoryBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    @Autowired
    public TransactionPostingService(CardXrefRepository cardXrefRepository,
                                     AccountRepository accountRepository,
                                     TransactionCategoryBalanceRepository categoryBalanceRepository,
                                     TransactionRepository transactionRepository) {
        this(cardXrefRepository, accountRepository, categoryBalanceRepository, transactionRepository,
                Clock.systemDefaultZone());
    }

    /** Test seam for the {@code FUNCTION CURRENT-DATE} call in Z-GET-DB2-FORMAT-TIMESTAMP. */
    TransactionPostingService(CardXrefRepository cardXrefRepository,
                              AccountRepository accountRepository,
                              TransactionCategoryBalanceRepository categoryBalanceRepository,
                              TransactionRepository transactionRepository,
                              Clock clock) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    /**
     * 1500-VALIDATE-TRAN. The account lookup runs only when the cross-reference lookup succeeded,
     * and — exactly as in the COBOL — the expiration check is evaluated after the credit limit
     * check, so an expired transaction that is also over limit is rejected with reason 103.
     */
    @Transactional(readOnly = true)
    public Optional<PostingRejectReason> validate(DailyTransaction dailyTransaction) {
        return validateInternal(dailyTransaction).reason();
    }

    /**
     * One iteration of the main loop: validate, then either post the transaction or build the
     * DALYREJS reject record.
     */
    @Transactional
    public PostingResult post(DailyTransaction dailyTransaction) {
        Validation validation = validateInternal(dailyTransaction);
        if (validation.reason().isPresent()) {
            return PostingResult.rejected(dailyTransaction, validation.reason().get());
        }

        Transaction transaction = toTransaction(dailyTransaction);
        updateCategoryBalance(validation.xref(), dailyTransaction);
        PostingRejectReason accountFailure = updateAccount(validation.account(), dailyTransaction);
        transactionRepository.save(transaction);
        return PostingResult.posted(dailyTransaction, transaction, accountFailure);
    }

    private Validation validateInternal(DailyTransaction dailyTransaction) {
        // 1500-A-LOOKUP-XREF
        CardXref xref = cardXrefRepository.findById(key(dailyTransaction.getCardNumber())).orElse(null);
        if (xref == null) {
            return new Validation(PostingRejectReason.INVALID_CARD_NUMBER, null, null);
        }

        // 1500-B-LOOKUP-ACCT
        Account account = accountRepository.findById(xref.getAccountId()).orElse(null);
        if (account == null) {
            return new Validation(PostingRejectReason.ACCOUNT_NOT_FOUND, xref, null);
        }

        PostingRejectReason reason = null;
        BigDecimal tempBalance = CobolUtils.nvl(account.getCurrentCycleCredit())
                .subtract(CobolUtils.nvl(account.getCurrentCycleDebit()))
                .add(CobolUtils.nvl(dailyTransaction.getAmount()));
        if (CobolUtils.nvl(account.getCreditLimit()).compareTo(tempBalance) < 0) {
            reason = PostingRejectReason.OVERLIMIT_TRANSACTION;
        }
        if (expirationDate(account).compareTo(originDate(dailyTransaction)) < 0) {
            reason = PostingRejectReason.TRANSACTION_AFTER_EXPIRATION;
        }
        return new Validation(reason, xref, account);
    }

    /** 2000-POST-TRANSACTION: the DALYTRAN fields moved into the TRANSACT record. */
    private Transaction toTransaction(DailyTransaction t) {
        return Transaction.builder()
                .transactionId(t.getTransactionId())
                .typeCode(t.getTypeCode())
                .categoryCode(t.getCategoryCode())
                .source(t.getSource())
                .description(t.getDescription())
                .amount(CobolUtils.rounded(CobolUtils.nvl(t.getAmount()), 2))
                .merchantId(t.getMerchantId())
                .merchantName(t.getMerchantName())
                .merchantCity(t.getMerchantCity())
                .merchantZip(t.getMerchantZip())
                .cardNumber(t.getCardNumber())
                .originTimestamp(t.getOriginTimestamp())
                .processTimestamp(db2Timestamp())
                .build();
    }

    /**
     * 2700-UPDATE-TCATBAL: read TCATBALF on (XREF-ACCT-ID, DALYTRAN-TYPE-CD, DALYTRAN-CAT-CD) and
     * either create the record with the transaction amount (2700-A) or add the amount to the
     * existing balance (2700-B).
     */
    private void updateCategoryBalance(CardXref xref, DailyTransaction dailyTransaction) {
        TransactionCategoryBalanceId id = TransactionCategoryBalanceId.builder()
                .accountId(xref.getAccountId())
                .typeCode(dailyTransaction.getTypeCode())
                .categoryCode(dailyTransaction.getCategoryCode())
                .build();
        BigDecimal amount = CobolUtils.nvl(dailyTransaction.getAmount());
        TransactionCategoryBalance balance = categoryBalanceRepository.findById(id).orElse(null);
        if (balance == null) {
            log.debug("TCATBAL record not found for key : {}{}{}.. Creating.",
                    id.getAccountId(), id.getTypeCode(), id.getCategoryCode());
            balance = TransactionCategoryBalance.builder()
                    .id(id)
                    .balance(CobolUtils.rounded(amount, 2))
                    .build();
        } else {
            balance.setBalance(CobolUtils.rounded(CobolUtils.nvl(balance.getBalance()).add(amount), 2));
        }
        categoryBalanceRepository.save(balance);
    }

    /**
     * 2800-UPDATE-ACCOUNT-REC: the transaction amount is always added to ACCT-CURR-BAL and then,
     * signed, to either the cycle credit (amount &gt;= 0) or the cycle debit (amount &lt; 0).
     * A failing REWRITE only records reason 109; the transaction is still posted.
     */
    private PostingRejectReason updateAccount(Account account, DailyTransaction dailyTransaction) {
        BigDecimal amount = CobolUtils.nvl(dailyTransaction.getAmount());
        account.setCurrentBalance(CobolUtils.rounded(
                CobolUtils.nvl(account.getCurrentBalance()).add(amount), 2));
        if (amount.signum() >= 0) {
            account.setCurrentCycleCredit(CobolUtils.rounded(
                    CobolUtils.nvl(account.getCurrentCycleCredit()).add(amount), 2));
        } else {
            account.setCurrentCycleDebit(CobolUtils.rounded(
                    CobolUtils.nvl(account.getCurrentCycleDebit()).add(amount), 2));
        }

        if (!accountRepository.existsById(account.getAccountId())) {
            log.warn("Account {} could not be updated: {}", account.getAccountId(),
                    PostingRejectReason.ACCOUNT_REWRITE_FAILED.getDescription());
            return PostingRejectReason.ACCOUNT_REWRITE_FAILED;
        }
        accountRepository.save(account);
        return null;
    }

    /** Z-GET-DB2-FORMAT-TIMESTAMP. */
    private String db2Timestamp() {
        return LocalDateTime.now(clock).format(DB2_TIMESTAMP) + DB2_REST;
    }

    private static String key(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.trim();
    }

    /** ACCT-EXPIRAION-DATE PIC X(10), compared as an alphanumeric field. */
    private static String expirationDate(Account account) {
        return CobolUtils.padRight(account.getExpirationDate(), 10);
    }

    /** DALYTRAN-ORIG-TS (1:10) PIC X(10). */
    private static String originDate(DailyTransaction dailyTransaction) {
        String origin = dailyTransaction.getOriginTimestamp() == null
                ? "" : dailyTransaction.getOriginTimestamp();
        return CobolUtils.padRight(origin, 10);
    }

    private record Validation(PostingRejectReason failure, CardXref xref, Account account) {

        Optional<PostingRejectReason> reason() {
            return Optional.ofNullable(failure);
        }
    }
}
