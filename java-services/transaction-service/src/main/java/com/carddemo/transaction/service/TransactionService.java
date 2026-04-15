package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.TransactionCreateRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.ValidationException;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementing transaction business logic migrated from COBOL programs:
 * - COTRN00C: Transaction list with pagination (PROCESS-PAGE-FORWARD / PROCESS-PAGE-BACKWARD)
 * - COTRN01C: Transaction view (READ-TRANSACT-FILE)
 * - COTRN02C: Transaction add (ADD-TRANSACTION with validation)
 */
@Service
public class TransactionService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
    private static final int TRANSACTION_ID_LENGTH = 16;

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionTypeRepository transactionTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
    }

    /**
     * List transactions with pagination, optionally filtered by card number.
     * Mirrors COTRN00C PROCESS-PAGE-FORWARD which reads 10 records per page
     * from the TRANSACT VSAM file, with optional STARTBR positioning by key.
     */
    public TransactionListResponse listTransactions(String cardNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionId").ascending());

        Page<Transaction> transactionPage;
        if (cardNumber != null && !cardNumber.isBlank()) {
            transactionPage = transactionRepository.findByCardNumber(cardNumber, pageable);
        } else {
            transactionPage = transactionRepository.findAll(pageable);
        }

        List<TransactionDto> dtos = transactionPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new TransactionListResponse(
                dtos,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages()
        );
    }

    /**
     * View a single transaction by ID.
     * Mirrors COTRN01C READ-TRANSACT-FILE which reads by RIDFLD (TRAN-ID).
     * Returns DFHRESP(NOTFND) -> "Transaction ID NOT found..." on miss.
     */
    public TransactionDto getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction ID NOT found: " + transactionId));
        return toDto(transaction);
    }

    /**
     * Add a new transaction.
     * Mirrors COTRN02C ADD-TRANSACTION:
     * 1. Generate next sequential ID (MOVE HIGH-VALUES TO TRAN-ID, STARTBR, READPREV, ADD 1)
     * 2. Validate card number exists (READ-CCXREF-FILE)
     * 3. Validate type code exists
     * 4. Validate amount > 0
     * 5. Set timestamps to current time
     * 6. WRITE to TRANSACT file
     */
    @Transactional
    public TransactionDto createTransaction(TransactionCreateRequest request) {
        // Validate card number exists (mirrors COTRN02C READ-CCXREF-FILE / NOTFND check)
        if (!transactionRepository.existsByCardNumber(request.getCardNumber())) {
            throw new ValidationException("Card Number NOT found: " + request.getCardNumber());
        }

        // Validate type code exists (mirrors COTRN02C type code validation)
        if (!transactionTypeRepository.existsById(request.getTypeCode())) {
            throw new ValidationException("Invalid transaction type code: " + request.getTypeCode());
        }

        // Validate amount > 0
        if (request.getAmount() == null
                || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }

        // Generate next sequential transaction ID
        // Mirrors COBOL: MOVE HIGH-VALUES TO TRAN-ID, STARTBR, READPREV, ADD 1
        String nextId = generateNextTransactionId();

        String now = LocalDateTime.now().format(TIMESTAMP_FORMAT);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(nextId);
        transaction.setTypeCode(request.getTypeCode());
        transaction.setCategoryCode(request.getCategoryCode());
        transaction.setSource(request.getSource() != null ? request.getSource() : "");
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "");
        transaction.setAmount(request.getAmount());
        transaction.setMerchantId(request.getMerchantId());
        transaction.setMerchantName(request.getMerchantName() != null ? request.getMerchantName() : "");
        transaction.setMerchantCity(request.getMerchantCity() != null ? request.getMerchantCity() : "");
        transaction.setMerchantZip(request.getMerchantZip() != null ? request.getMerchantZip() : "");
        transaction.setCardNumber(request.getCardNumber());
        transaction.setOriginTimestamp(now);
        transaction.setProcessedTimestamp(now);

        Transaction saved = transactionRepository.save(transaction);
        return toDto(saved);
    }

    /**
     * Generate the next sequential transaction ID.
     * COBOL logic from COTRN02C ADD-TRANSACTION:
     *   MOVE HIGH-VALUES TO TRAN-ID
     *   PERFORM STARTBR-TRANSACT-FILE
     *   PERFORM READPREV-TRANSACT-FILE
     *   MOVE TRAN-ID TO WS-TRAN-ID-N
     *   ADD 1 TO WS-TRAN-ID-N
     */
    private String generateNextTransactionId() {
        return transactionRepository.findMaxTransactionId()
                .map(maxId -> {
                    try {
                        long next = Long.parseLong(maxId.trim()) + 1;
                        return String.format("%0" + TRANSACTION_ID_LENGTH + "d", next);
                    } catch (NumberFormatException e) {
                        return String.format("%0" + TRANSACTION_ID_LENGTH + "d", 1L);
                    }
                })
                .orElse(String.format("%0" + TRANSACTION_ID_LENGTH + "d", 1L));
    }

    private TransactionDto toDto(Transaction entity) {
        TransactionDto dto = new TransactionDto();
        dto.setTransactionId(entity.getTransactionId());
        dto.setTypeCode(entity.getTypeCode());
        dto.setCategoryCode(entity.getCategoryCode());
        dto.setSource(entity.getSource());
        dto.setDescription(entity.getDescription());
        dto.setAmount(entity.getAmount());
        dto.setMerchantId(entity.getMerchantId());
        dto.setMerchantName(entity.getMerchantName());
        dto.setMerchantCity(entity.getMerchantCity());
        dto.setMerchantZip(entity.getMerchantZip());
        dto.setCardNumber(entity.getCardNumber());
        dto.setOriginTimestamp(entity.getOriginTimestamp());
        dto.setProcessedTimestamp(entity.getProcessedTimestamp());
        return dto;
    }
}
