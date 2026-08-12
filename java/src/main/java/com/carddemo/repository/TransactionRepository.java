package com.carddemo.repository;

import com.carddemo.model.entity.Transaction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS TRANSACT access (keyed on TRAN-ID). */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findAllByOrderByTransactionId(Pageable pageable);

    List<Transaction> findByCardNumberOrderByTransactionId(String cardNumber);
}
