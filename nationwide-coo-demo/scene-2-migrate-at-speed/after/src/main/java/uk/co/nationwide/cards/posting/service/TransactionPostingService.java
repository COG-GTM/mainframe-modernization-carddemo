package uk.co.nationwide.cards.posting.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nationwide.cards.posting.api.PostTransactionRequest;
import uk.co.nationwide.cards.posting.api.PostTransactionResponse;
import uk.co.nationwide.cards.posting.domain.Account;
import uk.co.nationwide.cards.posting.domain.Card;
import uk.co.nationwide.cards.posting.domain.CardXref;
import uk.co.nationwide.cards.posting.domain.TranCategoryBalance;
import uk.co.nationwide.cards.posting.domain.Transaction;
import uk.co.nationwide.cards.posting.repository.AccountRepository;
import uk.co.nationwide.cards.posting.repository.CardRepository;
import uk.co.nationwide.cards.posting.repository.CardXrefRepository;
import uk.co.nationwide.cards.posting.repository.TranCategoryBalanceRepository;
import uk.co.nationwide.cards.posting.repository.TransactionRepository;

/**
 * Modernized form of CBTRN02C's posting pipeline. One Java method replaces the
 * four COBOL paragraphs that together post a transaction:
 *
 * <pre>
 *   COBOL paragraph                       Java line range
 *   ----------------------------------    ----------------
 *   1500-VALIDATE-TRAN                    validate(...)
 *   2000-POST-TRANSACTION                 postTransaction(...) body
 *   2700-UPDATE-TCATBAL                   updateCategoryBalance(...)
 *   2800-UPDATE-ACCOUNT-REC               updateAccountBalance(...)
 *   2900-WRITE-TRANSACTION-FILE           transactionRepo.save(...)
 *   2500-WRITE-REJECT-REC                 TransactionRejectedException
 * </pre>
 *
 * Business rules preserved verbatim from the COBOL:
 * <ol>
 *   <li>Card number must exist in the card cross-reference (xref).</li>
 *   <li>Account looked up via the xref must exist.</li>
 *   <li>Account must be active ('Y').</li>
 *   <li>Card must be active ('Y').</li>
 *   <li>Card must not be expired (expiration date strictly after today).</li>
 *   <li>Transaction amount + current balance must not exceed credit limit.</li>
 * </ol>
 */
@Service
public class TransactionPostingService {

    private final CardXrefRepository xrefRepo;
    private final CardRepository cardRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final TranCategoryBalanceRepository categoryBalanceRepo;
    private final Clock clock;

    public TransactionPostingService(
            CardXrefRepository xrefRepo,
            CardRepository cardRepo,
            AccountRepository accountRepo,
            TransactionRepository transactionRepo,
            TranCategoryBalanceRepository categoryBalanceRepo,
            Clock clock) {
        this.xrefRepo = xrefRepo;
        this.cardRepo = cardRepo;
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
        this.categoryBalanceRepo = categoryBalanceRepo;
        this.clock = clock;
    }

    @Transactional
    public PostTransactionResponse postTransaction(PostTransactionRequest request) {
        // Paragraph 1500-VALIDATE-TRAN: card -> xref -> account -> active -> expiry -> credit.
        CardXref xref = xrefRepo.findById(request.getTranCardNum())
                .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.CARD_NOT_FOUND_IN_XREF));

        Account account = accountRepo.findById(xref.getXrefAcctId())
                .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.ACCOUNT_NOT_FOUND));

        if (!"Y".equals(account.getAcctActiveStatus())) {
            throw new TransactionRejectedException(ValidationFailure.ACCOUNT_INACTIVE);
        }

        Card card = cardRepo.findById(request.getTranCardNum())
                .orElseThrow(() -> new TransactionRejectedException(ValidationFailure.CARD_NOT_FOUND_IN_XREF));

        if (!"Y".equals(card.getCardActiveStatus())) {
            throw new TransactionRejectedException(ValidationFailure.CARD_INACTIVE);
        }

        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        if (card.getCardExpirationDate() != null && !card.getCardExpirationDate().isAfter(today)) {
            throw new TransactionRejectedException(ValidationFailure.CARD_EXPIRED);
        }

        BigDecimal projectedBalance = account.getAcctCurrBal().add(request.getTranAmt());
        if (projectedBalance.compareTo(account.getAcctCreditLimit()) > 0) {
            throw new TransactionRejectedException(ValidationFailure.EXCEEDS_CREDIT_LIMIT);
        }

        // Paragraph 2000-POST-TRANSACTION: copy DALYTRAN-* fields onto TRAN-RECORD.
        Instant processedAt = clock.instant();
        Transaction txn = new Transaction();
        txn.setTranId(request.getTranId());
        txn.setTranTypeCd(request.getTranTypeCd());
        txn.setTranCatCd(request.getTranCatCd());
        txn.setTranSource(request.getTranSource());
        txn.setTranDesc(request.getTranDesc());
        txn.setTranAmt(request.getTranAmt());
        txn.setTranMerchantId(request.getTranMerchantId());
        txn.setTranMerchantName(request.getTranMerchantName());
        txn.setTranMerchantCity(request.getTranMerchantCity());
        txn.setTranMerchantZip(request.getTranMerchantZip());
        txn.setTranCardNum(request.getTranCardNum());
        txn.setTranOrigTs(request.getTranOrigTs());
        txn.setTranProcTs(processedAt);

        // Paragraph 2700-UPDATE-TCATBAL: increment the per-category balance.
        updateCategoryBalance(account.getAcctId(), request.getTranTypeCd(), request.getTranCatCd(), request.getTranAmt());

        // Paragraph 2800-UPDATE-ACCOUNT-REC: update the account balance.
        account.setAcctCurrBal(projectedBalance);
        accountRepo.save(account);

        // Paragraph 2900-WRITE-TRANSACTION-FILE: persist the transaction record.
        transactionRepo.save(txn);

        return new PostTransactionResponse(
                txn.getTranId(),
                account.getAcctId(),
                account.getAcctCurrBal(),
                processedAt);
    }

    private void updateCategoryBalance(Long acctId, String tranTypeCd, Integer tranCatCd, BigDecimal amount) {
        TranCategoryBalance.Key key = new TranCategoryBalance.Key(acctId, tranTypeCd, tranCatCd);
        Optional<TranCategoryBalance> existing = categoryBalanceRepo.findById(key);
        TranCategoryBalance bal = existing.orElseGet(() ->
                new TranCategoryBalance(acctId, tranTypeCd, tranCatCd, BigDecimal.ZERO));
        bal.setTranCatBal(bal.getTranCatBal().add(amount));
        categoryBalanceRepo.save(bal);
    }
}
