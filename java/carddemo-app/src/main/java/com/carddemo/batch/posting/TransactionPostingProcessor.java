package com.carddemo.batch.posting;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.batch.item.ItemProcessor;

/**
 * Validates a daily transaction and computes its posting effects — the Java port of the
 * {@code 1500-VALIDATE-TRAN} and {@code 2000-POST-TRANSACTION} logic of {@code CBTRN02C}.
 *
 * <p>Validation order and rejection reasons are a faithful port:</p>
 * <ol>
 *   <li>{@code 1500-A-LOOKUP-XREF}: card number must resolve in the XREF; else reason 100.</li>
 *   <li>{@code 1500-B-LOOKUP-ACCT}: account must exist; else reason 101. When it exists,
 *       {@code WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT};
 *       if {@code ACCT-CREDIT-LIMIT < WS-TEMP-BAL} reason 102 (overlimit), and — evaluated
 *       afterwards and therefore able to override 102 — if the account is expired
 *       ({@code ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)}) reason 103.</li>
 * </ol>
 *
 * <p>For a valid transaction it builds the {@link Transaction} to persist, and applies the
 * exact {@code ADD} arithmetic of {@code 2700-UPDATE-TCATBAL} / {@code 2800-UPDATE-ACCOUNT-REC}
 * in {@link BigDecimal} (no {@code double}/{@code float}). All monetary values keep scale 2,
 * matching the copybooks' {@code V99}.</p>
 *
 * <p>Runs with chunk size 1 (see {@link PostTranJobConfig}) so that the account/category
 * balances read here reflect every earlier posting in the run, reproducing the record-by-record
 * accumulation of the sequential COBOL loop.</p>
 */
public class TransactionPostingProcessor implements ItemProcessor<DailyTransaction, PostingResult> {

    /** DB2 timestamp layout produced by {@code Z-GET-DB2-FORMAT-TIMESTAMP}: 26 chars. */
    private static final DateTimeFormatter PROC_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository categoryBalanceRepository;
    private final Clock clock;

    public TransactionPostingProcessor(CardXrefRepository cardXrefRepository,
                                       AccountRepository accountRepository,
                                       TransactionCategoryBalanceRepository categoryBalanceRepository,
                                       Clock clock) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.clock = clock;
    }

    @Override
    public PostingResult process(DailyTransaction dt) {
        // 1500-A-LOOKUP-XREF
        CardXref xref = cardXrefRepository.findById(nullSafe(dt.getTranCardNum())).orElse(null);
        if (xref == null) {
            return PostingResult.rejected(dt, RejectReason.INVALID_CARD_NUMBER);
        }

        // 1500-B-LOOKUP-ACCT
        Account account = accountRepository.findById(nullSafe(xref.getXrefAcctId())).orElse(null);
        if (account == null) {
            return PostingResult.rejected(dt, RejectReason.ACCOUNT_NOT_FOUND);
        }

        RejectReason reason = validateAccount(account, dt);
        if (reason != null) {
            return PostingResult.rejected(dt, reason);
        }

        // 2000-POST-TRANSACTION
        Transaction txn = buildTransaction(dt);
        TransactionCategoryBalance balance = applyCategoryBalance(xref.getXrefAcctId(), dt);
        applyAccount(account, dt.getTranAmt());
        return PostingResult.posted(txn, account, balance);
    }

    /** {@code 1500-B-LOOKUP-ACCT}: credit-limit then expiration check (later wins). */
    private RejectReason validateAccount(Account account, DailyTransaction dt) {
        RejectReason reason = null;

        BigDecimal tempBal = scale2(account.getAcctCurrCycCredit())
                .subtract(scale2(account.getAcctCurrCycDebit()))
                .add(scale2(dt.getTranAmt()));
        // IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL CONTINUE ELSE reason 102
        if (scale2(account.getAcctCreditLimit()).compareTo(tempBal) < 0) {
            reason = RejectReason.OVERLIMIT;
        }

        // IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10) CONTINUE ELSE reason 103
        String origDate = origDate(dt.getTranOrigTs());
        String expiration = nullSafe(account.getAcctExpirationDate());
        if (expiration.compareTo(origDate) < 0) {
            reason = RejectReason.AFTER_EXPIRATION;
        }
        return reason;
    }

    /** {@code 2000-POST-TRANSACTION}: copy DALYTRAN fields to TRAN-RECORD, stamp TRAN-PROC-TS. */
    private Transaction buildTransaction(DailyTransaction dt) {
        Transaction t = new Transaction();
        t.setTranId(dt.getDalytranId());
        t.setTranTypeCd(dt.getTranTypeCd());
        t.setTranCatCd(dt.getTranCatCd());
        t.setTranSource(dt.getTranSource());
        t.setTranDesc(dt.getTranDesc());
        t.setTranAmt(scale2(dt.getTranAmt()));
        t.setTranMerchantId(dt.getTranMerchantId());
        t.setTranMerchantName(dt.getTranMerchantName());
        t.setTranMerchantCity(dt.getTranMerchantCity());
        t.setTranMerchantZip(dt.getTranMerchantZip());
        t.setTranCardNum(dt.getTranCardNum());
        t.setTranOrigTs(dt.getTranOrigTs());
        t.setTranProcTs(LocalDateTime.now(clock).format(PROC_TS));
        return t;
    }

    /**
     * {@code 2700-UPDATE-TCATBAL}: read the category balance by
     * (XREF-ACCT-ID, DALYTRAN-TYPE-CD, DALYTRAN-CAT-CD); create with {@code 0 + amt}
     * ({@code 2700-A}) when absent, otherwise {@code balance + amt} ({@code 2700-B}).
     */
    private TransactionCategoryBalance applyCategoryBalance(String acctId, DailyTransaction dt) {
        TransactionCategoryBalanceId id =
                new TransactionCategoryBalanceId(acctId, dt.getTranTypeCd(), dt.getTranCatCd());
        Optional<TransactionCategoryBalance> existing = categoryBalanceRepository.findById(id);

        TransactionCategoryBalance balance;
        BigDecimal current;
        if (existing.isPresent()) {
            balance = existing.get();
            current = scale2(balance.getTranCatBal());
        } else {
            balance = new TransactionCategoryBalance();
            balance.setId(id);
            current = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        balance.setTranCatBal(current.add(scale2(dt.getTranAmt())));
        return balance;
    }

    /**
     * {@code 2800-UPDATE-ACCOUNT-REC}: {@code ACCT-CURR-BAL += DALYTRAN-AMT}; a non-negative
     * amount adds to {@code ACCT-CURR-CYC-CREDIT}, a negative amount to {@code ACCT-CURR-CYC-DEBIT}.
     */
    private void applyAccount(Account account, BigDecimal rawAmount) {
        BigDecimal amount = scale2(rawAmount);
        account.setAcctCurrBal(scale2(account.getAcctCurrBal()).add(amount));
        if (amount.signum() >= 0) {
            account.setAcctCurrCycCredit(scale2(account.getAcctCurrCycCredit()).add(amount));
        } else {
            account.setAcctCurrCycDebit(scale2(account.getAcctCurrCycDebit()).add(amount));
        }
    }

    private static BigDecimal scale2(BigDecimal value) {
        BigDecimal v = (value == null) ? BigDecimal.ZERO : value;
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    /** DALYTRAN-ORIG-TS(1:10) — the yyyy-MM-dd date portion of the origination timestamp. */
    private static String origDate(String origTs) {
        String ts = nullSafe(origTs);
        return ts.length() >= 10 ? ts.substring(0, 10) : ts;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
