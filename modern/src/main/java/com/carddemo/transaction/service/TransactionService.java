package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.CardXrefEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.exception.InvalidRequestException;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service for transaction CRUD operations.
 *
 * COBOL Traceability:
 * - List transactions: Replaces COTRN00C.cbl (Txn CT00) STARTBR/READNEXT browse
 * - View transaction: Replaces COTRN01C.cbl (Txn CT01) single READ
 * - Create transaction: Replaces COTRN02C.cbl (Txn CT02) WRITE with ID generation
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List transactions with optional filters and pagination.
     *
     * COBOL Traceability: Replaces COTRN00C PROCESS-PAGE-FORWARD /
     * PROCESS-PAGE-BACKWARD paragraphs that use STARTBR/READNEXT/READPREV
     * to browse the TRANSACT VSAM file 10 records at a time.
     */
    @Transactional(readOnly = true)
    public TransactionListResponse listTransactions(int page, int size,
                                                     String accountId, String cardNumber,
                                                     String startDate, String endDate) {
        LocalDateTime startDateTime = parseStartDate(startDate);
        LocalDateTime endDateTime = parseEndDate(endDate);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionEntity> pageResult = transactionRepository.findWithFilters(
                accountId, cardNumber, startDateTime, endDateTime, pageable);

        var transactions = pageResult.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new TransactionListResponse(
                transactions,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    /**
     * Get a single transaction by ID.
     *
     * COBOL Traceability: Replaces COTRN01C READ-TRANSACT-FILE paragraph
     * which does EXEC CICS READ on TRANSACT by TRAN-ID key.
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        TransactionEntity entity = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found: " + transactionId));
        return toResponse(entity);
    }

    /**
     * Create a new transaction.
     *
     * COBOL Traceability: Replaces COTRN02C.cbl processing:
     * - VALIDATE-INPUT-KEY-FIELDS: Validates account/card via CCXREF/CXACAIX
     * - GENERATE-TRAN-ID: STARTBR with HIGH-VALUES / READPREV to get max ID
     * - ADD-TRANSACTION: EXEC CICS WRITE to TRANSACT
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Validate card-account relationship (replaces READ-CCXREF-FILE / READ-CXACAIX-FILE)
        String cardNumber = resolveCardNumber(request.accountId(), request.cardNumber());

        // Generate next transaction ID (replaces STARTBR HIGH-VALUES / READPREV)
        String nextId = generateNextTransactionId();

        // Build and save entity
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(nextId);
        entity.setTypeCode(request.typeCode());
        entity.setCategoryCode(request.categoryCode());
        entity.setSource(request.source() != null ? request.source() : "ONLINE");
        entity.setDescription(request.description());
        entity.setAmount(request.amount());
        entity.setMerchantId(request.merchantId());
        entity.setMerchantName(request.merchantName());
        entity.setMerchantCity(request.merchantCity());
        entity.setMerchantZip(request.merchantZip());
        entity.setCardNumber(cardNumber);
        entity.setPosted(false); // Mark unposted so DailyTransactionProcessor updates balances

        LocalDateTime now = LocalDateTime.now();
        entity.setOriginTimestamp(
                request.originDate() != null ? parseDateTime(request.originDate()) : now);
        entity.setProcessedTimestamp(
                request.processedDate() != null ? parseDateTime(request.processedDate()) : now);

        TransactionEntity saved = transactionRepository.save(entity);
        log.info("Created transaction: {}", saved.getTransactionId());
        return toResponse(saved);
    }

    /**
     * Resolve card number from account ID or card number.
     * Validates the card-account cross-reference.
     *
     * COBOL Traceability: Replaces COTRN02C VALIDATE-INPUT-KEY-FIELDS
     * which reads CXACAIX (by account) or CCXREF (by card) to validate.
     */
    String resolveCardNumber(String accountId, String cardNumber) {
        if (cardNumber != null && !cardNumber.isBlank()) {
            // Validate card exists in xref
            cardXrefRepository.findById(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Card number not found in cross-reference: " + cardNumber));
            return cardNumber;
        }
        if (accountId != null && !accountId.isBlank()) {
            CardXrefEntity xref = cardXrefRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account ID not found in cross-reference: " + accountId));
            return xref.getCardNumber();
        }
        throw new InvalidRequestException(
                "Account ID or Card Number must be provided");
    }

    /**
     * Generate the next transaction ID atomically using a database sequence.
     *
     * COBOL Traceability: Replaces COTRN02C's pattern of
     * STARTBR with HIGH-VALUES, READPREV to get max ID, then ADD 1.
     * Uses a DB sequence to guarantee uniqueness under concurrent access.
     */
    String generateNextTransactionId() {
        long nextNum = transactionRepository.nextTransactionIdFromSequence();
        return String.format("%016d", nextNum);
    }

    TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getTransactionId(),
                entity.getTypeCode(),
                entity.getCategoryCode(),
                entity.getSource(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getMerchantId(),
                entity.getMerchantName(),
                entity.getMerchantCity(),
                entity.getMerchantZip(),
                entity.getCardNumber(),
                entity.getOriginTimestamp(),
                entity.getProcessedTimestamp()
        );
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Invalid start date format. Use YYYY-MM-DD");
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Invalid end date format. Use YYYY-MM-DD");
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("Invalid date format: " + dateStr + ". Use YYYY-MM-DD");
        }
    }
}
