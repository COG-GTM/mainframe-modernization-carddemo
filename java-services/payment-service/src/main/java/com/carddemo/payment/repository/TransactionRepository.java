package com.carddemo.payment.repository;

import com.carddemo.payment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find the transaction with the highest (last) transaction ID.
     * Mirrors the COBOL logic of STARTBR with HIGH-VALUES then READPREV
     * to get the last record from the TRANSACT file.
     */
    @Query("SELECT t FROM Transaction t ORDER BY t.tranId DESC LIMIT 1")
    Optional<Transaction> findTopByOrderByTranIdDesc();
}
