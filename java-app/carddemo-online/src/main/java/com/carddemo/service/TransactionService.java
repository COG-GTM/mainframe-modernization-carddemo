package com.carddemo.service;

import com.carddemo.dto.TransactionCreateRequest;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction management service — replaces COTRN00C, COTRN01C, COTRN02C.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               DailyTransactionRepository dailyTransactionRepository,
                               CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List transactions by account ID with pagination.
     * Mirrors COTRN00C: browses TRANSACT by account ID.
     */
    public Page<Transaction> listTransactions(Long acctId, Pageable pageable) {
        return transactionRepository.findByAcctId(acctId, pageable);
    }

    /**
     * Get transaction by ID.
     * Mirrors COTRN01C.
     */
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }

    /**
     * Create a new daily transaction for batch processing.
     * Mirrors COTRN02C online: creates a DailyTransaction record.
     */
    @Transactional
    public DailyTransaction addTransaction(TransactionCreateRequest dto) {
        // Validate card exists
        if (!cardXrefRepository.existsById(dto.getCardNum())) {
            throw new IllegalArgumentException("Card not found: " + dto.getCardNum());
        }

        DailyTransaction dt = new DailyTransaction();
        dt.setTransactionId(generateTransactionId());
        dt.setCardNum(dto.getCardNum());
        dt.setTypeCd(dto.getTypeCd());
        dt.setCategoryCd(dto.getCategoryCd() != null ? dto.getCategoryCd() : 0);
        dt.setSource(dto.getSource());
        dt.setDescription(dto.getDescription());
        dt.setAmount(dto.getAmount());
        dt.setMerchantId(dto.getMerchantId());
        dt.setMerchantName(dto.getMerchantName());
        dt.setMerchantCity(dto.getMerchantCity());
        dt.setMerchantZip(dto.getMerchantZip());
        dt.setOrigTimestamp(LocalDateTime.now());

        return dailyTransactionRepository.save(dt);
    }

    /**
     * Generate a 16-character transaction ID.
     */
    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
