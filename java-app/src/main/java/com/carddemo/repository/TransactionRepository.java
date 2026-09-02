package com.carddemo.repository;

import com.carddemo.domain.Transaction;

import java.util.List;
import java.util.Optional;

/** TRANSACT (VSAM KSDS keyed on TRAN-ID). */
public interface TransactionRepository {

    Optional<Transaction> findById(String transactionId);

    List<Transaction> findByCardNumber(String cardNumber);

    List<Transaction> findAll();

    Transaction save(Transaction transaction);
}
