package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core transaction service.
 * Replaces the main business logic in COTRN02C.cbl, including:
 * - ADD-TRANSACTION paragraph (create new transaction)
 * - COPY-LAST-TRAN-DATA paragraph (PF5 - copy last transaction)
 * - Transaction ID generation (STARTBR HIGH-VALUES / READPREV / ADD 1)
 *
 * Also covers transaction listing (COTRN00C) and viewing (COTRN01C)
 * for a unified Transaction microservice.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardLookupService cardLookupService;
    private final DateValidationService dateValidationService;

    public TransactionService(TransactionRepository transactionRepository,
                              CardLookupService cardLookupService,
                              DateValidationService dateValidationService) {
        this.transactionRepository = transactionRepository;
        this.cardLookupService = cardLookupService;
        this.dateValidationService = dateValidationService;
    }

    /**
     * Creates a new transaction.
     * Replaces: ADD-TRANSACTION paragraph in COTRN02C.
     *
     * Flow:
     * 1. Resolve card number via account/card cross-reference lookup
     *    (replaces VALIDATE-INPUT-KEY-FIELDS)
     * 2. Validate dates (replaces CALL 'CSUTLDTC')
     * 3. Generate next sequential transaction ID
     *    (replaces STARTBR HIGH-VALUES / READPREV / ADD 1)
     * 4. Build and persist the transaction record
     *    (replaces WRITE-TRANSACT-FILE)
     *
     * @param request the transaction creation request
     * @return response with the created transaction details and generated ID
     */
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Step 1: Resolve card number (replaces VALIDATE-INPUT-KEY-FIELDS)
        String cardNumber = cardLookupService.resolveCardNumber(
                request.getAccountId(), request.getCardNumber());

        // Step 2: Validate dates (replaces CALL 'CSUTLDTC')
        dateValidationService.validateDate(request.getOriginationDate(), "Orig Date");
        dateValidationService.validateDate(request.getProcessingDate(), "Proc Date");

        // Step 3: Generate next transaction ID
        String nextId = generateNextTransactionId();

        // Step 4: Build and save entity (replaces MOVE statements + WRITE)
        Transaction txn = new Transaction();
        txn.setTranId(nextId);
        txn.setTranTypeCd(request.getTransactionTypeCd());
        txn.setTranCatCd(request.getTransactionCatCd());
        txn.setTranSource(request.getSource());
        txn.setTranDesc(request.getDescription());
        txn.setTranAmt(request.getAmount());
        txn.setTranCardNum(cardNumber);
        txn.setTranMerchantId(request.getMerchantId());
        txn.setTranMerchantName(request.getMerchantName());
        txn.setTranMerchantCity(request.getMerchantCity());
        txn.setTranMerchantZip(request.getMerchantZip());
        txn.setTranOrigTs(request.getOriginationDate().toString());
        txn.setTranProcTs(request.getProcessingDate().toString());

        transactionRepository.save(txn);

        return mapToResponse(txn,
                "Transaction added successfully. Your Tran ID is " + nextId.trim() + ".");
    }

    /**
     * Retrieves a transaction by ID.
     * Replaces: READ-TRANSACT-FILE paragraph in COTRN01C.
     *
     * @param transactionId the transaction ID to look up
     * @return the transaction details
     * @throws ResourceNotFoundException if the transaction ID is not found
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction ID NOT found"));
        return mapToResponse(txn, null);
    }

    /**
     * Lists transactions with pagination.
     * Replaces: PROCESS-PAGE-FORWARD / PROCESS-PAGE-BACKWARD
     * paragraphs in COTRN00C (STARTBR / READNEXT / READPREV browse pattern).
     *
     * @param page page number (0-based)
     * @param size page size (default 10, matching COBOL's 10-row display)
     * @return paginated list of transactions
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> listTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findAllByOrderByTranIdAsc(pageable)
                .map(txn -> mapToResponse(txn, null));
    }

    /**
     * Gets the most recent (last) transaction.
     * Replaces: COPY-LAST-TRAN-DATA paragraph in COTRN02C (PF5 handler).
     * COBOL flow: STARTBR at HIGH-VALUES, READPREV to get last record.
     *
     * @return the most recent transaction
     * @throws ResourceNotFoundException if no transactions exist
     */
    @Transactional(readOnly = true)
    public TransactionResponse getLastTransaction() {
        Transaction txn = transactionRepository.findTopByOrderByTranIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No transactions found"));
        return mapToResponse(txn, null);
    }

    /**
     * Generates the next sequential transaction ID.
     * Replaces the COBOL pattern:
     *   MOVE HIGH-VALUES TO TRAN-ID
     *   PERFORM STARTBR-TRANSACT-FILE
     *   PERFORM READPREV-TRANSACT-FILE
     *   PERFORM ENDBR-TRANSACT-FILE
     *   MOVE TRAN-ID TO WS-TRAN-ID-N
     *   ADD 1 TO WS-TRAN-ID-N
     *
     * Uses database query instead of VSAM browse for thread safety.
     *
     * @return the next transaction ID as a 16-character zero-padded string
     */
    private String generateNextTransactionId() {
        return transactionRepository.findTopByOrderByTranIdDesc()
                .map(last -> {
                    long lastId = Long.parseLong(last.getTranId().trim());
                    return String.format("%016d", lastId + 1);
                })
                .orElse("0000000000000001");
    }

    /**
     * Maps a Transaction entity to a TransactionResponse DTO.
     * Replaces the MOVE statements that populate BMS screen output fields
     * from TRAN-RECORD fields in COTRN01C and COTRN02C.
     */
    private TransactionResponse mapToResponse(Transaction txn, String message) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(txn.getTranId());
        response.setCardNumber(txn.getTranCardNum());
        response.setTransactionTypeCd(txn.getTranTypeCd());
        response.setTransactionCatCd(txn.getTranCatCd());
        response.setSource(txn.getTranSource());
        response.setDescription(txn.getTranDesc());
        response.setAmount(txn.getTranAmt());
        response.setOriginationDate(txn.getTranOrigTs());
        response.setProcessingDate(txn.getTranProcTs());
        response.setMerchantId(txn.getTranMerchantId());
        response.setMerchantName(txn.getTranMerchantName());
        response.setMerchantCity(txn.getTranMerchantCity());
        response.setMerchantZip(txn.getTranMerchantZip());
        response.setMessage(message);
        return response;
    }
}
