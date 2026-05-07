package com.carddemo.service;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalanceId;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link TransactionCategoryBalance} operations.
 * <p>
 * Encapsulates business logic previously implemented in COBOL batch programs:
 * <ul>
 *   <li>CBACT04C: Interest calculation — reads balances sequentially by account</li>
 *   <li>CBTRN02C: Transaction posting — reads/creates/updates balances by composite key</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionCategoryBalanceService {

    private final TransactionCategoryBalanceRepository repository;

    /**
     * Find a balance record by its composite key.
     * Replaces COBOL: READ TCATBAL-FILE INTO TRAN-CAT-BAL-RECORD.
     */
    public Optional<TransactionCategoryBalance> findById(TransactionCategoryBalanceId id) {
        return repository.findById(id);
    }

    /**
     * Find all balance records for a given account.
     * Replaces COBOL: sequential read of TCATBAL-FILE filtered by TRANCAT-ACCT-ID.
     */
    public List<TransactionCategoryBalance> findByAccountId(Long accountId) {
        return repository.findByIdAccountId(accountId);
    }

    /**
     * Find all balance records for a given transaction type code.
     */
    public List<TransactionCategoryBalance> findByTypeCode(String typeCode) {
        return repository.findByIdTypeCode(typeCode);
    }

    /**
     * Retrieve all transaction category balance records.
     */
    public List<TransactionCategoryBalance> findAll() {
        return repository.findAll();
    }

    /**
     * Create or update a transaction category balance record.
     * Replaces COBOL paragraphs 2700-A-CREATE-TCATBAL-REC and 2700-B-UPDATE-TCATBAL-REC
     * in CBTRN02C.
     */
    @Transactional
    public TransactionCategoryBalance save(TransactionCategoryBalance balance) {
        return repository.save(balance);
    }

    /**
     * Update the balance for a given composite key by adding a transaction amount.
     * Replaces COBOL: ADD DALYTRAN-AMT TO TRAN-CAT-BAL in CBTRN02C paragraph 2700-B.
     * If the record does not exist, a new one is created (mirrors COBOL 2700-A behavior).
     *
     * @param id     the composite key
     * @param amount the transaction amount to add (can be negative)
     * @return the updated balance record
     */
    @Transactional
    public TransactionCategoryBalance addToBalance(TransactionCategoryBalanceId id, BigDecimal amount) {
        TransactionCategoryBalance record = repository.findById(id)
                .orElseGet(() -> {
                    TransactionCategoryBalance newRecord = new TransactionCategoryBalance();
                    newRecord.setId(id);
                    newRecord.setBalance(BigDecimal.ZERO);
                    return newRecord;
                });
        record.setBalance(record.getBalance().add(amount));
        return repository.save(record);
    }

    /**
     * Delete a transaction category balance record by composite key.
     */
    @Transactional
    public void deleteById(TransactionCategoryBalanceId id) {
        repository.deleteById(id);
    }
}
