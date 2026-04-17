package com.carddemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.model.TranCatBalance;
import com.carddemo.model.TranCatBalanceId;

/**
 * Repository for transaction category balance data.
 *
 * Replaces: TCATBAL-FILE I-O in CBTRN02C (2700-UPDATE-TCATBAL):
 *   - READ by composite key (2700-UPDATE-TCATBAL)
 *   - WRITE for new records (2700-A-CREATE-TCATBAL-REC)
 *   - REWRITE for existing records (2700-B-UPDATE-TCATBAL-REC)
 *
 * Also read by CBACT04C (INTCALC) for interest calculation.
 */
@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {
}
