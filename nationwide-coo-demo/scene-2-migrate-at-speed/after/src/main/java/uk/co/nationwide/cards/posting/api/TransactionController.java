package uk.co.nationwide.cards.posting.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.nationwide.cards.posting.service.TransactionPostingService;
import uk.co.nationwide.cards.posting.service.TransactionRejectedException;

/**
 * REST API replacing CICS access to CBTRN02C's posting paragraph. The legacy
 * batch invokes <code>2000-POST-TRANSACTION</code> for each record on the
 * daily feed; this controller exposes the same operation as a single
 * transactional POST endpoint, so it can be driven from either a Spring Batch
 * job (replacing the COBOL batch) or directly from an upstream service.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionPostingService service;

    public TransactionController(TransactionPostingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PostTransactionResponse> post(@Valid @RequestBody PostTransactionRequest request) {
        PostTransactionResponse response = service.postTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(TransactionRejectedException.class)
    public ResponseEntity<RejectedResponse> handleRejection(TransactionRejectedException ex) {
        RejectedResponse body = new RejectedResponse(ex.failure().legacyCode(), ex.failure().description());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }
}
