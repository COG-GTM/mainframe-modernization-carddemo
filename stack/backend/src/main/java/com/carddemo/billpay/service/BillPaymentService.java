package com.carddemo.billpay.service;

import com.carddemo.billpay.domain.AccountEntity;
import com.carddemo.billpay.domain.CardXrefEntity;
import com.carddemo.billpay.domain.TransactionEntity;
import com.carddemo.billpay.repository.AccountRepository;
import com.carddemo.billpay.repository.CardXrefRepository;
import com.carddemo.billpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Bill Payment business logic — 1:1 migration of COBIL00C (TransID CB00).
 *
 * <p>The public {@link #process(String, String)} method reproduces the COBOL paragraph
 * {@code PROCESS-ENTER-KEY} (COBIL00C.cbl:154-244) exactly: validation order, confirm dispatch,
 * the "nothing to pay" guard, the payment path (id sequencing, transaction constants, balance
 * decrement) and the exact user-facing messages.</p>
 */
@Service
public class BillPaymentService {

    // Exact message literals from COBIL00C (parity-critical).
    static final String MSG_ACCT_EMPTY      = "Acct ID can NOT be empty...";
    static final String MSG_INVALID_CONFIRM = "Invalid value. Valid values are (Y/N)...";
    static final String MSG_NOTHING_TO_PAY  = "You have nothing to pay...";
    static final String MSG_CONFIRM_PROMPT  = "Confirm to make a bill payment...";
    static final String MSG_ACCT_NOT_FOUND  = "Account ID NOT found...";
    static final String MSG_TRAN_DUP        = "Tran ID already exist...";

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Balance inquiry: the COBOL path where ENTER is pressed with the confirm flag blank
     * (reads the account, shows the balance, and prompts for confirmation).
     */
    @Transactional
    public BillPaymentResult inquiry(String acctIdRaw) {
        return process(acctIdRaw, "");
    }

    /**
     * Full Bill Payment transaction — mirrors PROCESS-ENTER-KEY for the supplied confirm value.
     *
     * @param acctIdRaw the entered account id (may be null/blank)
     * @param confirmRaw the confirm flag: Y/y, N/n, blank, or invalid
     */
    @Transactional
    public BillPaymentResult process(String acctIdRaw, String confirmRaw) {
        String acctIdStr = acctIdRaw == null ? "" : acctIdRaw.trim();
        String confirm = confirmRaw == null ? "" : confirmRaw.trim();

        // WHEN ACTIDINI = SPACES OR LOW-VALUES  (COBIL00C.cbl:159-164)
        if (acctIdStr.isEmpty()) {
            return BillPaymentResult.error(MSG_ACCT_EMPTY, BillPaymentResult.Field.ACCT_ID);
        }

        Long acctId;
        try {
            acctId = Long.parseLong(acctIdStr);
        } catch (NumberFormatException e) {
            // Non-numeric account id cannot exist in the KSDS keyed by a numeric id → NOTFND path.
            return BillPaymentResult.error(MSG_ACCT_NOT_FOUND, BillPaymentResult.Field.ACCT_ID);
        }

        boolean confirmYes;
        // EVALUATE CONFIRMI  (COBIL00C.cbl:173-191)
        switch (confirm) {
            case "Y":
            case "y":
                confirmYes = true;
                break;
            case "N":
            case "n":
                // PERFORM CLEAR-CURRENT-SCREEN + set err flag → screen cleared, no data change.
                return BillPaymentResult.cleared();
            case "":
                confirmYes = false;
                break;
            default:
                return BillPaymentResult.error(MSG_INVALID_CONFIRM, BillPaymentResult.Field.CONFIRM);
        }

        // READ-ACCTDAT-FILE (COBIL00C.cbl:343-372) — read for both Y and blank confirm.
        Optional<AccountEntity> accountOpt = accountRepository.findById(acctId);
        if (accountOpt.isEmpty()) {
            return BillPaymentResult.error(MSG_ACCT_NOT_FOUND, BillPaymentResult.Field.ACCT_ID);
        }
        AccountEntity account = accountOpt.get();
        BigDecimal balance = account.getCurrBal() == null ? BigDecimal.ZERO : account.getCurrBal();

        // IF ACCT-CURR-BAL <= ZEROS ... 'You have nothing to pay...' (COBIL00C.cbl:198-205)
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BillPaymentResult.error(MSG_NOTHING_TO_PAY, BillPaymentResult.Field.ACCT_ID, balance, acctId);
        }

        if (!confirmYes) {
            // 'Confirm to make a bill payment...' (COBIL00C.cbl:236-240)
            return BillPaymentResult.info(MSG_CONFIRM_PROMPT, BillPaymentResult.Field.CONFIRM, balance, acctId);
        }

        // ---- Payment path (CONF-PAY-YES) : COBIL00C.cbl:210-235 ----
        // READ-CXACAIX-FILE for the card number.
        Optional<CardXrefEntity> xrefOpt = cardXrefRepository.findFirstByAcctIdOrderByCardNum(acctId);
        if (xrefOpt.isEmpty()) {
            return BillPaymentResult.error(MSG_ACCT_NOT_FOUND, BillPaymentResult.Field.ACCT_ID, balance, acctId);
        }
        String cardNum = xrefOpt.get().getCardNum();

        // STARTBR/READPREV/ENDBR → max id; ADD 1 (first ever → 1). 16-char zero padded.
        long newIdNum = transactionRepository.findMaxNumericId() + 1L;
        String newId = String.format("%016d", newIdNum);

        if (transactionRepository.existsById(newId)) {
            // DUPKEY/DUPREC branch (COBIL00C.cbl:533-539)
            return BillPaymentResult.error(MSG_TRAN_DUP, BillPaymentResult.Field.ACCT_ID, balance, acctId);
        }

        String ts = LocalDateTime.now().format(TS_FMT) + ".000000";

        TransactionEntity txn = new TransactionEntity();
        txn.setId(newId);
        txn.setTypeCode("02");                              // MOVE '02' TO TRAN-TYPE-CD
        txn.setCategoryCode(2);                             // MOVE 2 TO TRAN-CAT-CD
        txn.setSource("POS TERM");                          // MOVE 'POS TERM'
        txn.setDescription("BILL PAYMENT - ONLINE");        // MOVE 'BILL PAYMENT - ONLINE'
        txn.setAmount(balance);                             // MOVE ACCT-CURR-BAL TO TRAN-AMT
        txn.setCardNum(cardNum);                            // MOVE XREF-CARD-NUM
        txn.setMerchantId(999999999L);                      // MOVE 999999999
        txn.setMerchantName("BILL PAYMENT");                // MOVE 'BILL PAYMENT'
        txn.setMerchantCity("N/A");                         // MOVE 'N/A'
        txn.setMerchantZip("N/A");                          // MOVE 'N/A'
        txn.setOrigTs(ts);
        txn.setProcTs(ts);
        transactionRepository.save(txn);                    // WRITE-TRANSACT-FILE

        // COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT ; REWRITE (COBIL00C.cbl:234-235)
        BigDecimal newBalance = balance.subtract(balance);  // == 0, preserving scale
        account.setCurrBal(newBalance);
        accountRepository.save(account);

        // Success message (STRING ... COBIL00C.cbl:527-531) — note the two spaces after the period.
        String message = "Payment successful. " + " Your Transaction ID is " + newId + ".";
        return BillPaymentResult.success(message, newBalance, acctId, newId);
    }
}
