package com.cardemo.batch.repository;

import com.cardemo.batch.entity.TransactionRecord;
import com.cardemo.batch.entity.TransactionRecordKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TRNXFILE — transaction records.
 * Replaces CBSTM03B sequential/keyed reads on TRNX-FILE.
 * ORDER BY card_num, tran_id replaces the JCL SORT step.
 */
@Repository
public interface TransactionRecordRepository
        extends JpaRepository<TransactionRecord, TransactionRecordKey> {

    @Query("SELECT t FROM TransactionRecord t WHERE t.cardNum = :cardNum "
            + "ORDER BY t.cardNum, t.tranId")
    List<TransactionRecord> findByCardNumOrderByCardNumAscTranIdAsc(
            @Param("cardNum") String cardNum);

    @Query("SELECT DISTINCT t.cardNum FROM TransactionRecord t ORDER BY t.cardNum")
    List<String> findDistinctCardNums();
}
