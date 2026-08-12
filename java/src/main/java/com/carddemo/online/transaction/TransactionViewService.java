package com.carddemo.online.transaction;

import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionViewResponse;
import com.carddemo.repository.TransactionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * COBOL program: COTRN01C — "View a Transaction from TRANSACT file" (transaction CT01).
 *
 * <p>Files: TRANSACT (copybook CVTRA05Y). Screen: BMS mapset COTRN01, map COTRN1A.
 */
@Service
public class TransactionViewService {

    static final String MSG_TRAN_ID_EMPTY = "Tran ID can NOT be empty...";
    static final String MSG_TRAN_ID_NOT_FOUND = "Transaction ID NOT found...";

    private final TransactionRepository transactions;

    public TransactionViewService(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    /** PROCESS-ENTER-KEY followed by READ-TRANSACT-FILE. */
    public TransactionViewResponse view(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return TransactionViewResponse.builder()
                    .success(false)
                    .errorMessage(MSG_TRAN_ID_EMPTY)
                    .build();
        }

        Optional<Transaction> found = transactions.findById(transactionId.trim());
        if (found.isEmpty()) {
            return TransactionViewResponse.builder()
                    .success(false)
                    .errorMessage(MSG_TRAN_ID_NOT_FOUND)
                    .build();
        }

        Transaction transaction = found.get();
        return TransactionViewResponse.builder()
                .success(true)
                .transactionId(transaction.getTransactionId())
                .cardNumber(transaction.getCardNumber())
                .typeCode(transaction.getTypeCode())
                .categoryCode(transaction.getCategoryCode())
                .source(transaction.getSource())
                .amount(TransactionFormats.amount(transaction.getAmount()))
                .description(transaction.getDescription())
                .originTimestamp(transaction.getOriginTimestamp())
                .processTimestamp(transaction.getProcessTimestamp())
                .merchantId(transaction.getMerchantId())
                .merchantName(transaction.getMerchantName())
                .merchantCity(transaction.getMerchantCity())
                .merchantZip(transaction.getMerchantZip())
                .build();
    }
}
