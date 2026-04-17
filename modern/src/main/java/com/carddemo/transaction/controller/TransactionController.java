package com.carddemo.transaction.controller;

import com.carddemo.transaction.dto.BillPaymentRequest;
import com.carddemo.transaction.dto.BillPaymentResponse;
import com.carddemo.transaction.dto.CreateTransactionRequest;
import com.carddemo.transaction.dto.ReportRequest;
import com.carddemo.transaction.dto.ReportResponse;
import com.carddemo.transaction.dto.TransactionListResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.service.BillPaymentService;
import com.carddemo.transaction.service.ReportService;
import com.carddemo.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * REST controller for transaction management operations.
 *
 * COBOL Traceability: Replaces the following CICS online programs:
 * - COTRN00C (Txn CT00): Transaction list with pagination and filtering
 * - COTRN01C (Txn CT01): Transaction detail view (read-only)
 * - COTRN02C (Txn CT02): Transaction add with cross-reference validation
 * - COBIL00C (Txn CB00): Bill payment with atomic balance update
 * - CORPT00C (Txn CR00): Transaction report request submission
 *
 * The CICS pseudo-conversational model (SEND MAP / RECEIVE MAP / RETURN TRANSID)
 * is replaced by stateless REST endpoints with JSON request/response bodies.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Transaction management operations")
public class TransactionController {

    private final TransactionService transactionService;
    private final BillPaymentService billPaymentService;
    private final ReportService reportService;

    public TransactionController(TransactionService transactionService,
                                  BillPaymentService billPaymentService,
                                  ReportService reportService) {
        this.transactionService = transactionService;
        this.billPaymentService = billPaymentService;
        this.reportService = reportService;
    }

    /**
     * List transactions with optional filtering and pagination.
     *
     * COBOL Traceability: Replaces COTRN00C.cbl (Txn CT00).
     * The COBOL program uses STARTBR/READNEXT/READPREV on the TRANSACT VSAM file
     * to display 10 transactions per page. PF7/PF8 keys navigate pages.
     * This endpoint replaces that with standard REST pagination.
     */
    @GetMapping("/transactions")
    @Operation(summary = "List transactions",
            description = "Replaces COTRN00C (Txn CT00) — paginated transaction list with optional filters")
    public ResponseEntity<TransactionListResponse> listTransactions(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 10, matching COBOL's 10-row display)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by account ID")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Filter by card number")
            @RequestParam(required = false) String cardNumber,
            @Parameter(description = "Filter start date (YYYY-MM-DD)")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter end date (YYYY-MM-DD)")
            @RequestParam(required = false) String endDate) {

        TransactionListResponse response = transactionService.listTransactions(
                page, size, accountId, cardNumber, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction detail by ID.
     *
     * COBOL Traceability: Replaces COTRN01C.cbl (Txn CT01).
     * The COBOL program reads a single transaction from TRANSACT via
     * EXEC CICS READ with the transaction ID as key, then displays
     * all fields on a detail screen (read-only).
     */
    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction detail",
            description = "Replaces COTRN01C (Txn CT01) — read-only transaction detail view")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable String transactionId) {

        TransactionResponse response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new transaction.
     *
     * COBOL Traceability: Replaces COTRN02C.cbl (Txn CT02).
     * The COBOL program:
     * 1. Validates account/card via CXACAIX/CCXREF cross-reference
     * 2. Generates next transaction ID via STARTBR/READPREV on TRANSACT
     * 3. Validates date fields via CALL to CSUTLDTC utility
     * 4. Writes new record via EXEC CICS WRITE to TRANSACT
     */
    @PostMapping("/transactions")
    @Operation(summary = "Create transaction",
            description = "Replaces COTRN02C (Txn CT02) — add new transaction with auto-generated ID")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Process a bill payment.
     *
     * COBOL Traceability: Replaces COBIL00C.cbl (Txn CB00).
     * The COBOL program performs a multi-file atomic operation:
     * 1. READ ACCTDAT — get current account balance
     * 2. READ CXACAIX — look up card number for account
     * 3. STARTBR/READPREV TRANSACT — generate next transaction ID
     * 4. WRITE TRANSACT — create payment transaction record
     * 5. REWRITE ACCTDAT — update account balance (balance - payment)
     * All within a single CICS unit of work for atomicity.
     */
    @PostMapping("/transactions/payment")
    @Operation(summary = "Process bill payment",
            description = "Replaces COBIL00C (Txn CB00) — atomic bill payment with balance update")
    public ResponseEntity<BillPaymentResponse> processPayment(
            @Valid @RequestBody BillPaymentRequest request) {

        BillPaymentResponse response = billPaymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Submit a transaction report request.
     *
     * COBOL Traceability: Replaces CORPT00C.cbl (Txn CR00).
     * The COBOL program validates date range via CALL to CSUTLDTC,
     * then writes report parameters to a CICS Transient Data queue
     * (WRITEQ TD) which triggers batch report generation (CBTRN03C).
     * This endpoint persists the request and returns a report ID for
     * async tracking.
     */
    @PostMapping("/reports/transactions")
    @Operation(summary = "Request transaction report",
            description = "Replaces CORPT00C (Txn CR00) — submit async report generation request")
    public ResponseEntity<ReportResponse> requestReport(
            @Valid @RequestBody ReportRequest request) {

        ReportResponse response = reportService.submitReportRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
