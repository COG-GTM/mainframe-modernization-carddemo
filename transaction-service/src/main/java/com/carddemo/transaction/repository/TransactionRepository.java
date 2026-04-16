package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Transaction entity.
 * Replaces CICS file control operations on the TRANSACT VSAM file.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByTranIdGreaterThanEqualOrderByTranIdAsc(String tranId, Pageable pageable);

    Page<Transaction> findAllByOrderByTranIdAsc(Pageable pageable);

    Optional<Transaction> findFirstByOrderByTranIdDesc();
}
