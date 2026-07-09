package com.carddemo.batch.account;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.batch.item.ItemProcessor;

/**
 * Interest-calculation processor — the heart of {@code CBACT04C} (INTCALC).
 *
 * <p>For one account (with all its {@code TCATBAL} rows) it reproduces, in order:</p>
 * <ol>
 *   <li>{@code 1100-GET-ACCT-DATA} — read the account master (must exist, else the COBOL
 *       program abends; here an {@link IllegalStateException} is thrown).</li>
 *   <li>{@code 1110-GET-XREF-DATA} — read the card cross-reference to obtain the card number
 *       stamped onto each interest transaction.</li>
 *   <li>For every category balance: {@code 1200-GET-INTEREST-RATE} — look up the disclosure-group
 *       rate for {@code (acct-group-id, type-cd, cat-cd)}, falling back to the {@code DEFAULT}
 *       group when the account-specific row is missing.</li>
 *   <li>When the rate is non-zero: {@code 1300-COMPUTE-INTEREST} (see {@link InterestCalculator})
 *       and {@code 1300-B-WRITE-TX} — accumulate the monthly interest and build an interest
 *       {@link Transaction}.</li>
 *   <li>{@code 1050-UPDATE-ACCOUNT} — add the accumulated interest to {@code ACCT-CURR-BAL} and
 *       reset the current-cycle credit/debit to zero.</li>
 * </ol>
 *
 * <p>{@code 1400-COMPUTE-FEES} is a no-op in the COBOL source ("To be implemented"), so it is
 * intentionally not ported.</p>
 */
public class AccountInterestProcessor implements ItemProcessor<AccountInterestItem, AccountInterestResult> {

    /** COBOL {@code MOVE '01' TO TRAN-TYPE-CD}. */
    private static final String INTEREST_TRAN_TYPE_CD = "01";
    /** COBOL {@code MOVE '05' TO TRAN-CAT-CD}. */
    private static final int INTEREST_TRAN_CAT_CD = 5;
    /** COBOL {@code MOVE 'System' TO TRAN-SOURCE}. */
    private static final String INTEREST_TRAN_SOURCE = "System";
    /** COBOL {@code STRING 'Int. for a/c ' , ACCT-ID}. */
    private static final String INTEREST_DESC_PREFIX = "Int. for a/c ";
    /** COBOL {@code MOVE 0 TO TRAN-MERCHANT-ID} — {@code PIC 9(09)}. */
    private static final String INTEREST_MERCHANT_ID = "000000000";
    /** COBOL {@code MOVE 'DEFAULT' TO FD-DIS-ACCT-GROUP-ID}. */
    private static final String DEFAULT_GROUP_ID = "DEFAULT";

    /** DB2 timestamp image {@code YYYY-MM-DD-HH.MM.SS.mmmm00} (see Z-GET-DB2-FORMAT-TIMESTAMP). */
    private static final DateTimeFormatter DB2_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSS'00'");

    private final AccountRepository accountRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final CardXrefRepository cardXrefRepository;
    private final String parmDate;

    /** {@code WS-TRANID-SUFFIX} — a run-global, monotonically increasing transaction sequence. */
    private final AtomicLong tranIdSuffix = new AtomicLong(0);

    public AccountInterestProcessor(AccountRepository accountRepository,
                                    DisclosureGroupRepository disclosureGroupRepository,
                                    CardXrefRepository cardXrefRepository,
                                    String parmDate) {
        this.accountRepository = accountRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.parmDate = parmDate;
    }

    @Override
    public AccountInterestResult process(AccountInterestItem item) {
        Account account = accountRepository.findById(item.acctId())
                .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + item.acctId()));

        String cardNum = cardXrefRepository.findByXrefAcctId(item.acctId()).stream()
                .findFirst().map(CardXref::getXrefCardNum).orElse(null);

        BigDecimal totalInterest = InterestCalculator.zeroAmount();
        List<Transaction> transactions = new ArrayList<>();

        for (TransactionCategoryBalance balance : item.balances()) {
            String typeCd = balance.getId().getTrancatTypeCd();
            Integer catCd = balance.getId().getTrancatCd();

            BigDecimal rate = lookupInterestRate(account.getAcctGroupId(), typeCd, catCd);
            if (rate.signum() == 0) {
                continue;
            }

            BigDecimal monthlyInterest = InterestCalculator.monthlyInterest(balance.getTranCatBal(), rate);
            totalInterest = totalInterest.add(monthlyInterest);
            transactions.add(buildInterestTransaction(account, cardNum, monthlyInterest));
        }

        // 1050-UPDATE-ACCOUNT
        account.setAcctCurrBal(account.getAcctCurrBal().add(totalInterest));
        account.setAcctCurrCycCredit(InterestCalculator.zeroAmount());
        account.setAcctCurrCycDebit(InterestCalculator.zeroAmount());

        return new AccountInterestResult(account, transactions);
    }

    /**
     * {@code 1200-GET-INTEREST-RATE} / {@code 1200-A-GET-DEFAULT-INT-RATE}: the account-group rate,
     * or the {@code DEFAULT}-group rate when the account-specific disclosure row is missing.
     */
    private BigDecimal lookupInterestRate(String acctGroupId, String typeCd, Integer catCd) {
        Optional<DisclosureGroup> group =
                disclosureGroupRepository.findById(new DisclosureGroupId(acctGroupId, typeCd, catCd));
        if (group.isEmpty()) {
            group = disclosureGroupRepository.findById(new DisclosureGroupId(DEFAULT_GROUP_ID, typeCd, catCd));
        }
        DisclosureGroup resolved = group.orElseThrow(() -> new IllegalStateException(
                "DISCLOSURE GROUP RECORD MISSING for group=" + acctGroupId
                        + "/default, type=" + typeCd + ", cat=" + catCd));
        return resolved.getDisIntRate();
    }

    /** {@code 1300-B-WRITE-TX}: build the interest {@link Transaction} record. */
    private Transaction buildInterestTransaction(Account account, String cardNum, BigDecimal amount) {
        long suffix = tranIdSuffix.incrementAndGet();
        String timestamp = LocalDateTime.now().format(DB2_TS);

        Transaction tx = new Transaction();
        tx.setTranId(parmDate + String.format("%06d", suffix));
        tx.setTranTypeCd(INTEREST_TRAN_TYPE_CD);
        tx.setTranCatCd(INTEREST_TRAN_CAT_CD);
        tx.setTranSource(INTEREST_TRAN_SOURCE);
        tx.setTranDesc(INTEREST_DESC_PREFIX + account.getAcctId());
        tx.setTranAmt(amount);
        tx.setTranMerchantId(INTEREST_MERCHANT_ID);
        tx.setTranCardNum(cardNum);
        tx.setTranOrigTs(timestamp);
        tx.setTranProcTs(timestamp);
        return tx;
    }
}
