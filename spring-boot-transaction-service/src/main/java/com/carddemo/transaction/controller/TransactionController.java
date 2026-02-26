package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.CardLookupResponse;
import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.service.CardLookupService;
import com.carddemo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for transaction operations.
 * Replaces the CICS transaction programs:
 * - COTRN00C (CT00): List transactions  -> GET /api/v1/transactions
 * - COTRN01C (CT01): View transaction   -> GET /api/v1/transactions/{id}
 * - COTRN02C (CT02): Add transaction    -> POST /api/v1/transactions
 *
 * Also exposes card/account cross-reference lookups that were
 * embedded in COTRN02C's VALIDATE-INPUT-KEY-FIELDS paragraph.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Credit card transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;
    private final CardLookupService cardLookupService;

    public TransactionController(TransactionService transactionService,
                                 CardLookupService cardLookupService) {
        this.transactionService = transactionService;
        this.cardLookupService = cardLookupService;
    }

    /**
     * Create a new transaction.
     * Replaces: COTRN02C (CT02) - ADD-TRANSACTION paragraph.
     *
     * COBOL flow replaced:
     * 1. VALIDATE-INPUT-KEY-FIELDS (account/card lookup)
     * 2. VALIDATE-INPUT-DATA-FIELDS (field validation)
     * 3. ADD-TRANSACTION (generate ID, write record)
     * 4. WRITE-TRANSACT-FILE (persist to VSAM)
     */
    @PostMapping("/transactions")
    @Operation(summary = "Add a new transaction",
            description = "Creates a new credit card transaction. " +
                    "Provide either accountId or cardNumber to identify the card holder.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Account or card not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate transaction ID")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a transaction by ID.
     * Replaces: COTRN01C (CT01) - READ-TRANSACT-FILE paragraph.
     */
    @GetMapping("/transactions/{id}")
    @Operation(summary = "View a transaction",
            description = "Retrieves a transaction by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction ID not found")
    })
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable("id") @Parameter(description = "Transaction ID (up to 16 digits)") String id) {
        TransactionResponse response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    /**
     * List transactions with pagination.
     * Replaces: COTRN00C (CT00) - PROCESS-PAGE-FORWARD / PROCESS-PAGE-BACKWARD.
     * COBOL displayed 10 records per page; default page size is 10.
     */
    @GetMapping("/transactions")
    @Operation(summary = "List transactions",
            description = "Lists transactions with pagination. Default page size is 10, " +
                    "matching the original COBOL 3270 screen display.")
    public ResponseEntity<Page<TransactionResponse>> listTransactions(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Page size") int size) {
        Page<TransactionResponse> transactions = transactionService.listTransactions(page, size);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get the most recent transaction.
     * Replaces: COTRN02C - COPY-LAST-TRAN-DATA paragraph (PF5 key handler).
     * Used to pre-fill the add transaction form with the last transaction's data.
     */
    @GetMapping("/transactions/last")
    @Operation(summary = "Get the most recent transaction",
            description = "Retrieves the last transaction by ID order. " +
                    "Used to copy/pre-fill data from the previous transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Last transaction found"),
            @ApiResponse(responseCode = "404", description = "No transactions found")
    })
    public ResponseEntity<TransactionResponse> getLastTransaction() {
        TransactionResponse response = transactionService.getLastTransaction();
        return ResponseEntity.ok(response);
    }

    /**
     * Look up card information by account ID.
     * Replaces: COTRN02C - READ-CXACAIX-FILE paragraph.
     * (CXACAIX = VSAM alternate index on CCXREF file, keyed by account ID)
     */
    @GetMapping("/cards/by-account/{accountId}")
    @Operation(summary = "Look up card by account ID",
            description = "Resolves an account ID to its associated card number " +
                    "via the card cross-reference file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account ID not found")
    })
    public ResponseEntity<CardLookupResponse> lookupByAccountId(
            @PathVariable("accountId") @Parameter(description = "Account ID (up to 11 digits)") Long accountId) {
        CardLookupResponse response = cardLookupService.lookupByAccountId(accountId);
        return ResponseEntity.ok(response);
    }

    /**
     * Look up account information by card number.
     * Replaces: COTRN02C - READ-CCXREF-FILE paragraph.
     * (CCXREF = VSAM KSDS file, primary key = card number)
     */
    @GetMapping("/accounts/by-card/{cardNumber}")
    @Operation(summary = "Look up account by card number",
            description = "Resolves a card number to its associated account ID " +
                    "via the card cross-reference file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card number not found")
    })
    public ResponseEntity<CardLookupResponse> lookupByCardNumber(
            @PathVariable("cardNumber") @Parameter(description = "Card number (16 digits)") String cardNumber) {
        CardLookupResponse response = cardLookupService.lookupByCardNumber(cardNumber);
        return ResponseEntity.ok(response);
    }
}
