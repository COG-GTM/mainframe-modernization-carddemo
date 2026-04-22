package com.carddemo.payment.service;

import com.carddemo.payment.dto.PaymentRequest;
import com.carddemo.payment.dto.PaymentResponse;
import com.carddemo.payment.exception.PaymentException;
import com.carddemo.payment.exception.ResourceNotFoundException;
import com.carddemo.payment.model.Account;
import com.carddemo.payment.model.CardCrossReference;
import com.carddemo.payment.model.Transaction;
import com.carddemo.payment.repository.AccountRepository;
import com.carddemo.payment.repository.CardCrossReferenceRepository;
import com.carddemo.payment.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Payment service implementing the bill payment business logic.
 * Faithfully translates the COBOL program COBIL00C.cbl.
 *
 * COBOL flow:
 * 1. Read account record (ACCTDAT file) by account ID
 * 2. If balance <= 0, show "You have nothing to pay"
 * 3. On confirmation: read card cross-reference (CXACAIX) to get card number
 * 4. Generate next transaction ID by reading last record from TRANSACT file
 * 5. Create new transaction record with type '02', description 'BILL PAYMENT - ONLINE'
 * 6. Write transaction and update account balance to 0
 */
@Service
public class PaymentService {

    private static final String TRAN_TYPE_PAYMENT = "02";
    private static final int TRAN_CAT_PAYMENT = 2;
    private static final String TRAN_SOURCE = "POS TERM";
    private static final String TRAN_DESC = "BILL PAYMENT - ONLINE";
    private static final long MERCHANT_ID = 999999999L;
    private static final String MERCHANT_NAME = "BILL PAYMENT";
    private static final String MERCHANT_CITY = "N/A";
    private static final String MERCHANT_ZIP = "N/A";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardCrossReferenceRepository cardCrossReferenceRepository;

    public PaymentService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          CardCrossReferenceRepository cardCrossReferenceRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardCrossReferenceRepository = cardCrossReferenceRepository;
    }

    /**
     * Get account balance information.
     * Mirrors the COBOL READ-ACCTDAT-FILE paragraph.
     */
    public Account getAccountBalance(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID NOT found..."));
    }

    /**
     * Process a bill payment.
     * Faithfully translates the PROCESS-ENTER-KEY paragraph from COBIL00C.cbl.
     *
     * Steps (matching COBOL logic):
     * a. Read account -> if not found, throw ResourceNotFoundException
     * b. If currentBalance <= 0, throw PaymentException "You have nothing to pay"
     * c. Look up card cross-reference by account ID to get card number
     * d. Find highest existing transaction ID, increment by 1
     * e. Create transaction with type '02', category 2, source 'POS TERM',
     *    description 'BILL PAYMENT - ONLINE', amount = currentBalance
     * f. Save transaction, update account balance to 0 (balance - amount)
     * g. Return payment confirmation
     */
    @Transactional
    public PaymentResponse processBillPayment(PaymentRequest request) {
        // Step a: Read account (mirrors READ-ACCTDAT-FILE)
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID NOT found..."));

        BigDecimal currentBalance = account.getCurrentBalance();

        // Step b: Check balance (mirrors: IF ACCT-CURR-BAL <= ZEROS)
        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("You have nothing to pay...");
        }

        // Step c: Read card cross-reference (mirrors READ-CXACAIX-FILE)
        CardCrossReference xref = cardCrossReferenceRepository
                .findByAccountId(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID NOT found..."));

        // Step d: Generate next transaction ID
        // Mirrors: MOVE HIGH-VALUES TO TRAN-ID, STARTBR, READPREV, ADD 1 TO WS-TRAN-ID-NUM
        String newTranId = generateNextTransactionId();

        // Step e: Create transaction record (mirrors INITIALIZE TRAN-RECORD and field assignments)
        String timestamp = getCurrentTimestamp();

        Transaction transaction = new Transaction();
        transaction.setTranId(newTranId);
        transaction.setTypeCode(TRAN_TYPE_PAYMENT);
        transaction.setCategoryCode(TRAN_CAT_PAYMENT);
        transaction.setSource(TRAN_SOURCE);
        transaction.setDescription(TRAN_DESC);
        transaction.setAmount(currentBalance);
        transaction.setCardNumber(xref.getCardNumber());
        transaction.setMerchantId(MERCHANT_ID);
        transaction.setMerchantName(MERCHANT_NAME);
        transaction.setMerchantCity(MERCHANT_CITY);
        transaction.setMerchantZip(MERCHANT_ZIP);
        transaction.setOriginTimestamp(timestamp);
        transaction.setProcessTimestamp(timestamp);

        // Step f: Write transaction and update account balance
        // Mirrors: WRITE-TRANSACT-FILE then COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
        transactionRepository.save(transaction);

        account.setCurrentBalance(currentBalance.subtract(currentBalance));
        accountRepository.save(account);

        // Step g: Return confirmation
        // Mirrors: 'Payment successful. Your Transaction ID is ...'
        return new PaymentResponse(
                newTranId,
                account.getAcctId(),
                currentBalance,
                currentBalance,
                BigDecimal.ZERO,
                "Payment successful. Your Transaction ID is " + newTranId.trim() + "."
        );
    }

    /**
     * Generate the next transaction ID.
     * Mirrors the COBOL logic:
     *   MOVE HIGH-VALUES TO TRAN-ID
     *   PERFORM STARTBR-TRANSACT-FILE
     *   PERFORM READPREV-TRANSACT-FILE
     *   MOVE TRAN-ID TO WS-TRAN-ID-NUM
     *   ADD 1 TO WS-TRAN-ID-NUM
     */
    private String generateNextTransactionId() {
        Optional<Transaction> lastTransaction = transactionRepository.findTopByOrderByTranIdDesc();

        long nextId;
        if (lastTransaction.isPresent()) {
            try {
                nextId = Long.parseLong(lastTransaction.get().getTranId().trim()) + 1;
            } catch (NumberFormatException e) {
                nextId = 1L;
            }
        } else {
            // Mirrors COBOL: WHEN DFHRESP(ENDFILE) -> MOVE ZEROS TO TRAN-ID, then ADD 1
            nextId = 1L;
        }

        // COBOL PIC 9(16) - pad to 16 digits
        return String.format("%016d", nextId);
    }

    /**
     * Get current timestamp in the format used by the COBOL program.
     * Mirrors GET-CURRENT-TIMESTAMP paragraph.
     * Format: YYYY-MM-DD HH:MM:SS.000000
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return now.format(formatter);
    }
}
