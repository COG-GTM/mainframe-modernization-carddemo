package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.BillPaymentRequest;
import com.carddemo.transaction.dto.BillPaymentResponse;
import com.carddemo.transaction.entity.AccountEntity;
import com.carddemo.transaction.entity.CardXrefEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for bill payment processing.
 *
 * COBOL Traceability: Replaces COBIL00C.cbl (Txn CB00).
 * The COBOL program performs a multi-file update in a single CICS unit of work:
 * 1. READ ACCTDAT (account balance)
 * 2. READ CXACAIX (card cross-reference by account)
 * 3. STARTBR/READPREV TRANSACT (generate next transaction ID)
 * 4. WRITE TRANSACT (create payment transaction record)
 * 5. REWRITE ACCTDAT (update account balance)
 *
 * The @Transactional annotation ensures atomicity, replacing the implicit
 * CICS unit-of-work rollback on ABEND.
 */
@Service
public class BillPaymentService {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentService.class);

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
     * Process a bill payment atomically.
     *
     * COBOL Traceability: Replaces COBIL00C PROCESS-ENTER-KEY paragraph
     * when CONF-PAY-YES is set. The original performs:
     * - READ-ACCTDAT-FILE to get current balance
     * - READ-CXACAIX-FILE to get card number for account
     * - STARTBR/READPREV on TRANSACT to generate next ID
     * - WRITE-TRANSACT-FILE to create payment record
     * - UPDATE-ACCTDAT-FILE to reduce balance (ACCT-CURR-BAL - TRAN-AMT)
     */
    @Transactional
    public BillPaymentResponse processPayment(BillPaymentRequest request) {
        // Step 1: Read account (replaces READ-ACCTDAT-FILE)
        AccountEntity account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account ID NOT found: " + request.accountId()));

        BigDecimal currentBalance = account.getCurrentBalance();

        // Validate balance (replaces COBIL00C's check: IF ACCT-CURR-BAL <= ZEROS)
        if (currentBalance == null || currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("You have nothing to pay");
        }

        BigDecimal paymentAmount = request.amount();
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Payment amount must be positive");
        }
        if (paymentAmount.compareTo(currentBalance) > 0) {
            throw new InvalidRequestException(
                    "Payment amount exceeds current balance of " + currentBalance);
        }

        // Step 2: Look up card via cross-reference (replaces READ-CXACAIX-FILE)
        String cardNumber;
        if (request.cardNumber() != null && !request.cardNumber().isBlank()) {
            cardNumber = request.cardNumber();
        } else {
            CardXrefEntity xref = cardXrefRepository.findByAccountId(request.accountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Card cross-reference not found for account: " + request.accountId()));
            cardNumber = xref.getCardNumber();
        }

        // Step 3: Generate next transaction ID atomically from DB sequence
        // (replaces STARTBR HIGH-VALUES / READPREV)
        long nextNum = transactionRepository.nextTransactionIdFromSequence();
        String transactionId = String.format("%016d", nextNum);

        // Step 4: Create payment transaction (replaces WRITE-TRANSACT-FILE)
        // Maps the hardcoded values from COBIL00C lines 220-232
        LocalDateTime now = LocalDateTime.now();
        TransactionEntity txn = new TransactionEntity();
        txn.setTransactionId(transactionId);
        txn.setTypeCode("02");                           // MOVE '02' TO TRAN-TYPE-CD
        txn.setCategoryCode(2);                           // MOVE 2 TO TRAN-CAT-CD
        txn.setSource("POS TERM");                        // MOVE 'POS TERM' TO TRAN-SOURCE
        txn.setDescription("BILL PAYMENT - ONLINE");      // MOVE 'BILL PAYMENT - ONLINE' TO TRAN-DESC
        txn.setAmount(paymentAmount);                     // MOVE ACCT-CURR-BAL TO TRAN-AMT
        txn.setCardNumber(cardNumber);                    // MOVE XREF-CARD-NUM TO TRAN-CARD-NUM
        txn.setMerchantId(999999999L);                    // MOVE 999999999 TO TRAN-MERCHANT-ID
        txn.setMerchantName("BILL PAYMENT");              // MOVE 'BILL PAYMENT' TO TRAN-MERCHANT-NAME
        txn.setMerchantCity("N/A");                       // MOVE 'N/A' TO TRAN-MERCHANT-CITY
        txn.setMerchantZip("N/A");                        // MOVE 'N/A' TO TRAN-MERCHANT-ZIP
        txn.setOriginTimestamp(now);                       // MOVE WS-TIMESTAMP TO TRAN-ORIG-TS
        txn.setProcessedTimestamp(now);                    // MOVE WS-TIMESTAMP TO TRAN-PROC-TS
        transactionRepository.save(txn);

        // Step 5: Update account balance (replaces UPDATE-ACCTDAT-FILE / REWRITE)
        // COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
        BigDecimal newBalance = currentBalance.subtract(paymentAmount);
        account.setCurrentBalance(newBalance);
        accountRepository.save(account);

        log.info("Bill payment processed: txn={}, account={}, amount={}, newBalance={}",
                transactionId, request.accountId(), paymentAmount, newBalance);

        return new BillPaymentResponse(
                transactionId,
                request.accountId(),
                paymentAmount,
                currentBalance,
                newBalance,
                "Bill payment processed successfully"
        );
    }
}
