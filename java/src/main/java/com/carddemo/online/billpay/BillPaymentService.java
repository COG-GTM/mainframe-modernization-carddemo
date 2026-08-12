package com.carddemo.online.billpay;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.TransactionBrowseRepository;
import com.carddemo.online.transaction.TransactionFormats;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: COBIL00C — "Pay bill of the account in full" (transaction CB00).
 *
 * <p>Files: ACCTDAT (copybook CVACT01Y), CXACAIX (copybook CVACT03Y), TRANSACT (copybook
 * CVTRA05Y). Screen: BMS mapset COBIL00, map COBIL0A.
 *
 * <p>A confirmed payment writes one transaction for the whole current balance (type code 02,
 * category 2, source POS TERM, merchant 999999999 "BILL PAYMENT") and then rewrites the account
 * with {@code ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT}. COBIL00C touches no other account field:
 * the cycle credit and debit totals are deliberately left alone, as the batch posting programs own
 * them.
 */
@Service
public class BillPaymentService {

    static final String MSG_ACCT_EMPTY = "Acct ID can NOT be empty...";
    static final String MSG_CONFIRM_INVALID = "Invalid value. Valid values are (Y/N)...";
    static final String MSG_ACCT_NOT_FOUND = "Account ID NOT found...";
    static final String MSG_NOTHING_TO_PAY = "You have nothing to pay...";
    static final String MSG_CONFIRM = "Confirm to make a bill payment...";
    static final String MSG_TRAN_ID_EXISTS = "Tran ID already exist...";

    /** TRAN-TYPE-CD moved by PROCESS-ENTER-KEY. */
    static final String PAYMENT_TYPE_CODE = "02";

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AccountRepository accounts;
    private final CardXrefRepository cardXrefs;
    private final TransactionRepository transactions;
    private final TransactionBrowseRepository transactionBrowse;
    private final Clock clock;

    @Autowired
    public BillPaymentService(
            AccountRepository accounts,
            CardXrefRepository cardXrefs,
            TransactionRepository transactions,
            TransactionBrowseRepository transactionBrowse) {
        this(accounts, cardXrefs, transactions, transactionBrowse, Clock.systemDefaultZone());
    }

    /** EXEC CICS ASKTIME / FORMATTIME is driven by an injectable clock so tests stay deterministic. */
    BillPaymentService(
            AccountRepository accounts,
            CardXrefRepository cardXrefs,
            TransactionRepository transactions,
            TransactionBrowseRepository transactionBrowse,
            Clock clock) {
        this.accounts = accounts;
        this.cardXrefs = cardXrefs;
        this.transactions = transactions;
        this.transactionBrowse = transactionBrowse;
        this.clock = clock;
    }

    /** PROCESS-ENTER-KEY. */
    @Transactional
    public BillPaymentResponse pay(BillPaymentRequest request) {
        String accountIdInput = request.getAccountId() == null ? "" : request.getAccountId().trim();
        if (accountIdInput.isEmpty()) {
            return failure(MSG_ACCT_EMPTY, null);
        }

        String confirm = request.getConfirm() == null ? "" : request.getConfirm().trim();
        boolean confirmed;
        if (confirm.equalsIgnoreCase("Y")) {
            confirmed = true;
        } else if (confirm.equalsIgnoreCase("N")) {
            // CLEAR-CURRENT-SCREEN then MOVE 'Y' TO WS-ERR-FLG: the screen is wiped, no message.
            return failure(null, null);
        } else if (confirm.isEmpty()) {
            confirmed = false;
        } else {
            return failure(MSG_CONFIRM_INVALID, null);
        }

        // ACTIDIN is moved to ACCT-ID PIC 9(11); a value that is not a number can never match a key.
        Optional<Account> found = isNumeric(accountIdInput)
                ? accounts.findById(Long.parseLong(accountIdInput))
                : Optional.empty();
        if (found.isEmpty()) {
            return failure(MSG_ACCT_NOT_FOUND, null);
        }
        Account account = found.get();
        BigDecimal balance = CobolUtils.nvl(account.getCurrentBalance());

        if (balance.signum() <= 0) {
            return failure(MSG_NOTHING_TO_PAY, balance);
        }

        if (!confirmed) {
            return failure(MSG_CONFIRM, balance);
        }

        CardXref xref = cardXrefs.findFirstByAccountId(account.getAccountId()).orElse(null);
        if (xref == null) {
            return failure(MSG_ACCT_NOT_FOUND, balance);
        }

        String transactionId = nextTransactionId();
        if (transactions.existsById(transactionId)) {
            return failure(MSG_TRAN_ID_EXISTS, balance);
        }

        // MOVE ACCT-CURR-BAL TO TRAN-AMT: the whole outstanding balance is paid in one transaction.
        BigDecimal paidAmount = balance;
        String timestamp = LocalDateTime.now(clock).format(TIMESTAMP) + ".000000";
        transactions.save(Transaction.builder()
                .transactionId(transactionId)
                .typeCode(PAYMENT_TYPE_CODE)
                .categoryCode(2)
                .source("POS TERM")
                .description("BILL PAYMENT - ONLINE")
                .amount(paidAmount)
                .cardNumber(xref.getCardNumber())
                .merchantId(999999999L)
                .merchantName("BILL PAYMENT")
                .merchantCity("N/A")
                .merchantZip("N/A")
                .originTimestamp(timestamp)
                .processTimestamp(timestamp)
                .build());

        BigDecimal newBalance = balance.subtract(paidAmount);
        account.setCurrentBalance(newBalance);
        accounts.save(account);

        return BillPaymentResponse.builder()
                .success(true)
                .message("Payment successful.  Your Transaction ID is " + transactionId + ".")
                .currentBalanceDisplay(currentBalance(newBalance))
                .currentBalance(newBalance)
                .transactionId(transactionId)
                .paidAmount(paidAmount)
                .build();
    }

    private static boolean isNumeric(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    /** STARTBR on HIGH-VALUES + READPREV + 1; an empty file leaves TRAN-ID at zeros. */
    private String nextTransactionId() {
        long highest = transactionBrowse
                .findFirstByOrderByTransactionIdDesc()
                .map(transaction -> Long.parseLong(transaction.getTransactionId().trim()))
                .orElse(0L);
        return TransactionFormats.transactionId(highest + 1);
    }

    private BillPaymentResponse failure(String message, BigDecimal balance) {
        return BillPaymentResponse.builder()
                .success(false)
                .message(message)
                .currentBalance(balance)
                .currentBalanceDisplay(balance == null ? null : currentBalance(balance))
                .build();
    }

    /** Renders WS-CURR-BAL PIC +9999999999.99. */
    static String currentBalance(BigDecimal value) {
        BigDecimal scaled = CobolUtils.nvl(value).setScale(2, RoundingMode.HALF_UP);
        String digits = scaled.abs().unscaledValue().toString();
        if (digits.length() < 3) {
            digits = "0".repeat(3 - digits.length()) + digits;
        }
        String integerPart = digits.substring(0, digits.length() - 2);
        String decimalPart = digits.substring(digits.length() - 2);
        if (integerPart.length() < 10) {
            integerPart = "0".repeat(10 - integerPart.length()) + integerPart;
        } else if (integerPart.length() > 10) {
            integerPart = integerPart.substring(integerPart.length() - 10);
        }
        return (scaled.signum() < 0 ? "-" : "+") + integerPart + "." + decimalPart;
    }
}
