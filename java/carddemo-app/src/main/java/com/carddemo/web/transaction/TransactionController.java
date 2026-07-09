package com.carddemo.web.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.transaction.TransactionNotFoundException;
import com.carddemo.service.transaction.TransactionService;
import com.carddemo.service.transaction.TransactionValidationException;
import com.carddemo.web.transaction.dto.TransactionAddRequest;
import com.carddemo.web.transaction.dto.TransactionAddResponse;
import com.carddemo.web.transaction.dto.TransactionDetailDto;
import com.carddemo.web.transaction.dto.TransactionErrorResponse;
import com.carddemo.web.transaction.dto.TransactionListResponse;

/**
 * REST surface for the online transaction screens ({@code COTRN00C}/{@code COTRN01C}/
 * {@code COTRN02C}). Requires an authenticated principal (CS-2 security); the CICS
 * pseudo-conversational flow is also reachable through the navigation framework via the
 * {@link com.carddemo.web.transaction.TransactionListScreenHandler list},
 * {@link com.carddemo.web.transaction.TransactionViewScreenHandler view} and
 * {@link com.carddemo.web.transaction.TransactionAddScreenHandler add} screen handlers.
 *
 * <ul>
 *   <li>{@code GET  /api/transactions} — paged browse ({@code COTRN00}).</li>
 *   <li>{@code GET  /api/transactions/{tranId}} — view one ({@code COTRN01}).</li>
 *   <li>{@code POST /api/transactions} — add with full validation ({@code COTRN02}).</li>
 * </ul>
 *
 * <p>Validation failures carry the verbatim COBOL message: 400 for input/validation
 * ({@link TransactionValidationException}), 404 for a missing transaction
 * ({@link TransactionNotFoundException}). The handler is controller-local (not a global
 * {@code @ControllerAdvice}).</p>
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public TransactionListResponse list(
            @RequestParam(name = "startTranId", required = false) String startTranId,
            @RequestParam(name = "pageSize", required = false, defaultValue = "0") int pageSize) {
        return transactionService.list(startTranId, pageSize);
    }

    @GetMapping("/{tranId}")
    public TransactionDetailDto view(@PathVariable("tranId") String tranId) {
        return transactionService.view(tranId);
    }

    @PostMapping
    public ResponseEntity<TransactionAddResponse> add(@RequestBody TransactionAddRequest request) {
        TransactionAddResponse response = transactionService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<TransactionErrorResponse> handleValidation(TransactionValidationException ex) {
        return ResponseEntity.badRequest().body(new TransactionErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<TransactionErrorResponse> handleNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new TransactionErrorResponse(ex.getMessage()));
    }
}
