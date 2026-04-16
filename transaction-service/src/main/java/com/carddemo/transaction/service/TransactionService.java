package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.*;
import com.carddemo.transaction.entity.Account;
import com.carddemo.transaction.entity.CardXref;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.exception.AccountNotFoundException;
import com.carddemo.transaction.exception.CardNotFoundException;
import com.carddemo.transaction.exception.TransactionNotFoundException;
import com.carddemo.transaction.exception.TransactionValidationException;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.validation.TransactionValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction service implementing all business logic.
 * Ports: COTRN00C (list), COTRN01C (view), COTRN02C (add), COBIL00C (bill payment).
 */
@Service
public class TransactionService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionIdGenerator idGenerator;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionIdGenerator idGenerator) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * List transactions with pagination.
     * Port of COTRN00C: STARTBR/READNEXT with 10 per page.
     *
     * @param page     page number (0-based)
     * @param size     page size (default 10)
     * @param tranIdFilter optional filter by transaction ID prefix (must be numeric)
     */
    @Transactional(readOnly = true)
    public TransactionListResponse listTransactions(int page, Integer size, String tranIdFilter) {
        int pageSize = (size != null && size > 0) ? size : DEFAULT_PAGE_SIZE;

        if (tranIdFilter != null && !tranIdFilter.isBlank()) {
            // Validate filter is numeric (COTRN00C: 'Tran ID must be Numeric ...')
            if (!tranIdFilter.chars().allMatch(Character::isDigit)) {
                throw new TransactionValidationException("Tran ID must be Numeric ...");
            }
            Page<Transaction> result = transactionRepository
                    .findByTranIdGreaterThanEqualOrderByTranIdAsc(
                            tranIdFilter,
                            PageRequest.of(page, pageSize));
            return toListResponse(result);
        }

        Page<Transaction> result = transactionRepository
                .findAllByOrderByTranIdAsc(PageRequest.of(page, pageSize));
        return toListResponse(result);
    }

    /**
     * View a single transaction by ID.
     * Port of COTRN01C: READ TRANSACT by TRAN-ID.
     */
    @Transactional(readOnly = true)
    public TransactionResponse viewTransaction(String tranId) {
        if (tranId == null || tranId.isBlank()) {
            throw new TransactionValidationException("Tran ID can NOT be empty...");
        }
        Transaction transaction = transactionRepository.findById(tranId)
                .orElseThrow(() -> new TransactionNotFoundException(tranId));
        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * Add a new transaction with full validation.
     * Port of COTRN02C: VALIDATE-INPUT-KEY-FIELDS -> VALIDATE-INPUT-DATA-FIELDS -> ADD-TRANSACTION.
     */
    @Transactional
    public TransactionResponse addTransaction(TransactionAddRequest request) {
        // Validate confirmation
        TransactionValidator.validateConfirmation(request.confirmed());

        // Validate key fields (account/card)
        TransactionValidator.validateKeyFields(request);

        // Cross-reference lookup
        String cardNumber = resolveCardNumber(request);

        // Validate data fields
        TransactionValidator.validateDataFields(request);

        // Generate sequential ID
        String tranId = idGenerator.generateNextId();

        // Build transaction record
        Transaction transaction = new Transaction();
        transaction.setTranId(tranId);
        transaction.setTranTypeCd(request.tranTypeCd());
        transaction.setTranCatCd(Integer.parseInt(request.tranCatCd()));
        transaction.setTranSource(request.tranSource());
        transaction.setTranDesc(request.tranDesc());
        transaction.setTranAmt(new BigDecimal(request.tranAmt()));
        transaction.setTranCardNum(cardNumber);
        transaction.setTranMerchantId(Long.parseLong(request.merchantId()));
        transaction.setTranMerchantName(request.merchantName());
        transaction.setTranMerchantCity(request.merchantCity());
        transaction.setTranMerchantZip(request.merchantZip());
        transaction.setTranOrigTs(request.tranOrigDate());
        transaction.setTranProcTs(request.tranProcDate());

        transaction = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(transaction);
    }

    /**
     * Process bill payment: create transaction + update account balance atomically.
     * Port of COBIL00C: two-phase write with WRITE TRANSACT + REWRITE ACCTDAT.
     */
    @Transactional
    public BillPaymentResponse processBillPayment(BillPaymentRequest request) {
        // Validate account ID
        if (request.accountId() == null || request.accountId().isBlank()) {
            throw new TransactionValidationException("Acct ID can NOT be empty...");
        }

        // Validate confirmation
        if (!request.confirmed()) {
            throw new TransactionValidationException(
                    "Confirm to make a bill payment...");
        }

        long acctId;
        try {
            acctId = Long.parseLong(request.accountId());
        } catch (NumberFormatException e) {
            throw new TransactionValidationException("Account ID must be Numeric...");
        }

        // Read account (COBIL00C: READ-ACCTDAT-FILE)
        Account account = accountRepository.findById(acctId)
                .orElseThrow(AccountNotFoundException::new);

        // Check balance (COBIL00C: ACCT-CURR-BAL <= ZEROS)
        if (account.getAcctCurrBal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException("You have nothing to pay...");
        }

        // Lookup card number from cross-reference (COBIL00C: READ-CXACAIX-FILE)
        CardXref xref = cardXrefRepository.findByXrefAcctId(acctId)
                .orElseThrow(AccountNotFoundException::new);

        // Generate transaction ID (READPREV from HIGH-VALUES, add 1)
        String tranId = idGenerator.generateNextId();

        // Build bill payment transaction record (COBIL00C field assignments)
        BigDecimal paymentAmount = account.getAcctCurrBal();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        Transaction transaction = new Transaction();
        transaction.setTranId(tranId);
        transaction.setTranTypeCd("02");
        transaction.setTranCatCd(2);
        transaction.setTranSource("POS TERM");
        transaction.setTranDesc("BILL PAYMENT - ONLINE");
        transaction.setTranAmt(paymentAmount);
        transaction.setTranCardNum(xref.getXrefCardNum());
        transaction.setTranMerchantId(999999999L);
        transaction.setTranMerchantName("BILL PAYMENT");
        transaction.setTranMerchantCity("N/A");
        transaction.setTranMerchantZip("N/A");
        transaction.setTranOrigTs(timestamp);
        transaction.setTranProcTs(timestamp);

        // Atomic: write transaction + update balance
        transactionRepository.save(transaction);

        // COBIL00C: COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
        BigDecimal newBalance = account.getAcctCurrBal().subtract(paymentAmount);
        account.setAcctCurrBal(newBalance);
        accountRepository.save(account);

        String message = "Payment successful. Your Transaction ID is " +
                tranId.stripLeading() + ".";

        return new BillPaymentResponse(tranId, paymentAmount, newBalance, message);
    }

    /**
     * Resolves card number from request, doing cross-reference lookups as needed.
     * If accountId provided: look up card via CXACAIX (account->card).
     * If cardNumber provided: look up account via CCXREF (card->account).
     */
    private String resolveCardNumber(TransactionAddRequest request) {
        boolean hasAccountId = request.accountId() != null && !request.accountId().isBlank();
        boolean hasCardNumber = request.cardNumber() != null && !request.cardNumber().isBlank();

        if (hasAccountId) {
            long acctId = Long.parseLong(request.accountId());
            CardXref xref = cardXrefRepository.findByXrefAcctId(acctId)
                    .orElseThrow(AccountNotFoundException::new);
            return xref.getXrefCardNum();
        } else if (hasCardNumber) {
            CardXref xref = cardXrefRepository.findByXrefCardNum(request.cardNumber())
                    .orElseThrow(CardNotFoundException::new);
            return xref.getXrefCardNum();
        }
        throw new TransactionValidationException(
                "Account or Card Number must be entered...");
    }

    private TransactionListResponse toListResponse(Page<Transaction> page) {
        return new TransactionListResponse(
                page.getContent().stream()
                        .map(TransactionResponse::fromEntity)
                        .toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
