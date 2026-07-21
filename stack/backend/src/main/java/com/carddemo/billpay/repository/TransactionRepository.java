package com.carddemo.billpay.repository;

import com.carddemo.billpay.domain.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** TRANSACT KSDS browse-for-max-key + insert (COBIL00C: STARTBR/READPREV/ENDBR, WRITE-TRANSACT-FILE). */
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    /**
     * Highest existing numeric transaction id, or 0 when the table is empty.
     * Mirrors the COBOL descending browse (READPREV) that yields the max TRAN-ID,
     * and the ENDFILE branch that starts numbering at zero.
     */
    @Query("select coalesce(max(cast(t.id as long)), 0L) from TransactionEntity t")
    long findMaxNumericId();
}
