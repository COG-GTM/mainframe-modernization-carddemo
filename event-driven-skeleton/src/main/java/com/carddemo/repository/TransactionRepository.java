package com.carddemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.model.Transaction;

/**
 * Repository for transaction master data.
 *
 * Replaces: TRANSACT-FILE WRITE in CBTRN02C (2900-WRITE-TRANSACTION-FILE).
 *
 * The findByCardNumOrderByTranId method replaces the multi-step process in
 * CREASTMT.JCL:
 *   - STEP010 (SORT by card-number + tran-id)
 *   - STEP020 (REPRO to TRXFL.VSAM.KSDS)
 *   - CBSTM03A 8500-READTRNX-READ (sequential read of sorted transactions)
 *
 * By querying directly with ORDER BY, we eliminate the need for the
 * intermediate TRXFL temporary VSAM file entirely.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find all transactions for a given card, ordered by transaction ID.
     *
     * Replaces: CREASTMT STEP010 SORT FIELDS=(263,16,CH,A,1,16,CH,A)
     * which sorts TRANSACT VSAM by card-number (ascending) then tran-id (ascending).
     */
    List<Transaction> findByCardNumOrderByTranId(String cardNum);
}
