package com.carddemo.service.billpay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.web.billpay.dto.BillPayRequest;
import com.carddemo.web.billpay.dto.BillPayResponse;

/**
 * Online bill payment ported from {@code app/cbl/COBIL00C.cbl} ({@code PROCESS-ENTER-KEY}).
 *
 * <p>Pays an account balance in full: it creates a "BILL PAYMENT - ONLINE" payment
 * {@link Transaction} for the current balance and draws the {@link Account} balance down to
 * zero, exactly reproducing the COBOL validations, ordering and messages. All money maths use
 * {@link BigDecimal} at scale 2 and the COBOL truncating {@code COMPUTE} semantics
 * ({@link RoundingMode#DOWN}).</p>
 *
 * <p>Legacy transaction-id allocation ({@code STARTBR}/{@code READPREV} for the highest
 * {@code TRAN-ID}, then {@code +1}) becomes "max existing id + 1", zero-padded to the 16-char
 * {@code TRAN-ID} width; an empty file yields {@code 0000000000000001}.</p>
 */
@Service
public class BillPayService {

    /** {@code MOVE '02' TO TRAN-TYPE-CD}. */
    static final String PAYMENT_TRAN_TYPE = "02";
    /** {@code MOVE 2 TO TRAN-CAT-CD}. */
    static final int PAYMENT_TRAN_CAT = 2;
    /** {@code MOVE 'POS TERM' TO TRAN-SOURCE}. */
    static final String PAYMENT_SOURCE = "POS TERM";
    /** {@code MOVE 'BILL PAYMENT - ONLINE' TO TRAN-DESC}. */
    static final String PAYMENT_DESC = "BILL PAYMENT - ONLINE";
    /** {@code MOVE 999999999 TO TRAN-MERCHANT-ID}. */
    static final String PAYMENT_MERCHANT_ID = "999999999";
    /** {@code MOVE 'BILL PAYMENT' TO TRAN-MERCHANT-NAME}. */
    static final String PAYMENT_MERCHANT_NAME = "BILL PAYMENT";
    /** {@code MOVE 'N/A' TO TRAN-MERCHANT-CITY / TRAN-MERCHANT-ZIP}. */
    static final String PAYMENT_MERCHANT_NA = "N/A";
    /** TRAN-ID PIC X(16) — zero-padded numeric id width. */
    static final int TRAN_ID_WIDTH = 16;
    /** TRAN-ORIG-TS / TRAN-PROC-TS PIC X(26) — {@code YYYY-MM-DD HH:MM:SS.mmmmmm}. */
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public BillPayService(AccountRepository accountRepository,
            CardXrefRepository cardXrefRepository,
            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Perform one {@code PROCESS-ENTER-KEY} turn of {@code COBIL00C}.
     *
     * @throws BillPayException for the COBOL error branches (empty id, invalid confirm,
     *         account not found, nothing to pay).
     */
    @Transactional
    public BillPayResponse pay(BillPayRequest request) {
        String accountId = request == null ? null : trimToNull(request.accountId());
        String confirm = request == null ? null : trimToNull(request.confirm());

        // WHEN ACTIDINI = SPACES OR LOW-VALUES
        if (accountId == null) {
            throw new BillPayException(BillPayMessages.EMPTY_ACCT_ID, HttpStatus.BAD_REQUEST);
        }

        Confirm choice = Confirm.of(confirm);

        // EVALUATE CONFIRMI ... WHEN 'N'/'n' -> CLEAR-CURRENT-SCREEN (no account read).
        if (choice == Confirm.NO) {
            return BillPayResponse.declined(accountId, null);
        }
        // WHEN OTHER -> "Invalid value. Valid values are (Y/N)..."
        if (choice == Confirm.INVALID) {
            throw new BillPayException(BillPayMessages.INVALID_CONFIRM, HttpStatus.BAD_REQUEST);
        }

        // PERFORM READ-ACCTDAT-FILE (UPDATE) — WHEN DFHRESP(NOTFND) -> "Account ID NOT found..."
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BillPayException(
                        BillPayMessages.ACCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        BigDecimal balance = scale(account.getAcctCurrBal());

        // IF ACCT-CURR-BAL <= ZEROS -> "You have nothing to pay..."
        if (balance.signum() <= 0) {
            throw new BillPayException(BillPayMessages.NOTHING_TO_PAY,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // CONF-PAY-NO branch: show the balance and ask for confirmation.
        if (choice == Confirm.NONE) {
            return BillPayResponse.confirmationRequired(accountId, balance,
                    BillPayMessages.CONFIRM_PAYMENT);
        }

        // CONF-PAY-YES: create the payment transaction and draw the balance to zero.
        CardXref xref = cardXrefRepository.findByXrefAcctId(accountId).stream().findFirst()
                .orElseThrow(() -> new BillPayException(
                        BillPayMessages.ACCT_NOT_FOUND, HttpStatus.NOT_FOUND));

        String transactionId = nextTransactionId();
        String timestamp = LocalDateTime.now().format(TIMESTAMP);

        Transaction payment = new Transaction();
        payment.setTranId(transactionId);
        payment.setTranTypeCd(PAYMENT_TRAN_TYPE);
        payment.setTranCatCd(PAYMENT_TRAN_CAT);
        payment.setTranSource(PAYMENT_SOURCE);
        payment.setTranDesc(PAYMENT_DESC);
        payment.setTranAmt(balance);
        payment.setTranCardNum(xref.getXrefCardNum());
        payment.setTranMerchantId(PAYMENT_MERCHANT_ID);
        payment.setTranMerchantName(PAYMENT_MERCHANT_NAME);
        payment.setTranMerchantCity(PAYMENT_MERCHANT_NA);
        payment.setTranMerchantZip(PAYMENT_MERCHANT_NA);
        payment.setTranOrigTs(timestamp);
        payment.setTranProcTs(timestamp);
        transactionRepository.save(payment);

        // COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT (truncating), then REWRITE.
        BigDecimal newBalance = balance.subtract(balance).setScale(2, RoundingMode.DOWN);
        account.setAcctCurrBal(newBalance);
        accountRepository.save(account);

        return BillPayResponse.paid(accountId, balance, newBalance, transactionId,
                BillPayMessages.paymentSuccessful(transactionId));
    }

    /**
     * Allocate the next {@code TRAN-ID}: highest existing numeric id + 1, zero-padded to
     * {@link #TRAN_ID_WIDTH}. Mirrors the {@code STARTBR}/{@code READPREV}/{@code +1} block
     * (empty file → {@code ADD 1 TO ZEROS} → {@code 1}).
     */
    private String nextTransactionId() {
        List<Transaction> all = transactionRepository.findAll();
        long max = 0L;
        for (Transaction t : all) {
            String id = t.getTranId();
            if (id != null && !id.isBlank()) {
                try {
                    max = Math.max(max, Long.parseLong(id.trim()));
                } catch (NumberFormatException ignored) {
                    // Non-numeric ids are not part of the online sequence; skip them.
                }
            }
        }
        return String.format("%0" + TRAN_ID_WIDTH + "d", max + 1);
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.DOWN);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** The {@code EVALUATE CONFIRMI} cases of {@code COBIL00C}. */
    private enum Confirm {
        YES, NO, NONE, INVALID;

        static Confirm of(String value) {
            if (value == null) {
                return NONE;
            }
            return switch (value) {
                case "Y", "y" -> YES;
                case "N", "n" -> NO;
                default -> INVALID;
            };
        }
    }
}
