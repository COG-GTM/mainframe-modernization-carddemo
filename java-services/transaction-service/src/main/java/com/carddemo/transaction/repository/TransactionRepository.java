package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByCardNumber(String cardNumber, Pageable pageable);

    /**
     * Mirrors COBOL ADD-TRANSACTION logic from COTRN02C:
     * MOVE HIGH-VALUES TO TRAN-ID, STARTBR, READPREV to get the last (highest) ID,
     * then ADD 1 to generate the next sequential transaction ID.
     */
    @Query("SELECT MAX(t.transactionId) FROM Transaction t")
    Optional<String> findMaxTransactionId();

    boolean existsByCardNumber(String cardNumber);
}
