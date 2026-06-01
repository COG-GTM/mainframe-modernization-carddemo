package com.carddemo.repository;

import com.carddemo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Transaction} ({@code TRANSACT} sequential output).
 *
 * <p>CBACT04C writes interest transactions sequentially (paragraph {@code 1300-B-WRITE-TX}); this
 * maps to {@link JpaRepository#save(Object)}.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
