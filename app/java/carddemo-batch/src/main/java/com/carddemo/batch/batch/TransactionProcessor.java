package com.carddemo.batch.batch;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.CardXref;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.RejectedTransaction;
import com.carddemo.batch.service.TransactionPostingService;
import com.carddemo.batch.service.TransactionValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Optional;

/**
 * Spring Batch ItemProcessor that validates daily transactions and either
 * posts them (valid) or writes them to the reject file (invalid).
 *
 * Valid transactions are posted inside this processor via the posting service.
 * The processor always returns null so the chunk writer is effectively a no-op.
 */
public class TransactionProcessor implements ItemProcessor<DailyTransaction, DailyTransaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    private final TransactionValidationService validationService;
    private final TransactionPostingService postingService;
    private final RejectWriter rejectWriter;

    public TransactionProcessor(TransactionValidationService validationService,
                                TransactionPostingService postingService,
                                RejectWriter rejectWriter) {
        this.validationService = validationService;
        this.postingService = postingService;
        this.rejectWriter = rejectWriter;
    }

    @Override
    public DailyTransaction process(DailyTransaction txn) throws Exception {
        Optional<RejectedTransaction> rejection = validationService.validate(txn);

        if (rejection.isPresent()) {
            log.info("Transaction {} rejected: {} - {}",
                    txn.getTransactionId(),
                    rejection.get().getReasonCode(),
                    rejection.get().getReasonDescription());
            rejectWriter.write(rejection.get());
            return null;
        }

        // Valid transaction: look up xref and account again for posting
        CardXref xref = validationService.lookupXref(txn.getCardNumber())
                .orElseThrow(() -> new IllegalStateException(
                        "XREF not found for card " + txn.getCardNumber() + " after validation passed"));
        Account account = validationService.lookupAccount(xref.getAccountId())
                .orElseThrow(() -> new IllegalStateException(
                        "Account not found for ID " + xref.getAccountId() + " after validation passed"));

        postingService.post(txn, xref, account);

        log.info("Transaction {} posted successfully", txn.getTransactionId());

        // Return null so the chunk writer does not attempt to write again
        return null;
    }
}
