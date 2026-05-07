package com.carddemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.entity.Transaction;
import com.carddemo.repository.TransactionRepository;

/**
 * Service layer for {@link Transaction} operations.
 * <p>
 * Encapsulates business logic previously spread across multiple COBOL programs:
 * <ul>
 *   <li>COTRN00C — list transactions (STARTBR/READNEXT/READPREV)</li>
 *   <li>COTRN01C — view single transaction (READ)</li>
 *   <li>COTRN02C — add transaction (WRITE)</li>
 *   <li>COBIL00C — billing (READ/REWRITE/WRITE)</li>
 *   <li>CBTRN01C — batch read</li>
 *   <li>CBTRN02C — batch processing</li>
 * </ul>
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findById(String id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCardNum(String cardNum) {
        return transactionRepository.findByCardNum(cardNum);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByTypeCode(String typeCode) {
        return transactionRepository.findByTypeCode(typeCode);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByMerchantId(Long merchantId) {
        return transactionRepository.findByMerchantId(merchantId);
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteById(String id) {
        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return transactionRepository.count();
    }
}
