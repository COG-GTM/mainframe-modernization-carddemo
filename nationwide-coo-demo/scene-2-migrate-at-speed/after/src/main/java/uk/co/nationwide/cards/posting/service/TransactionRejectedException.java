package uk.co.nationwide.cards.posting.service;

/**
 * Thrown by {@link TransactionPostingService} when a transaction fails any of
 * the validation checks. The presence of this exception is the modern form of
 * the COBOL paragraph <code>2500-WRITE-REJECT-REC</code> which writes the
 * failed record to the daily reject file (DALYREJS).
 */
public class TransactionRejectedException extends RuntimeException {

    private final ValidationFailure failure;

    public TransactionRejectedException(ValidationFailure failure) {
        super(failure.description());
        this.failure = failure;
    }

    public ValidationFailure failure() {
        return failure;
    }
}
