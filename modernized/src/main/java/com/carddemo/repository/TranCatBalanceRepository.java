package com.carddemo.repository;

import com.carddemo.model.TranCatBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for {@link TranCatBalance} ({@code TCATBALF} KSDS).
 *
 * <p>CBACT04C reads this file sequentially in account-key order (paragraph
 * {@code 1000-TCATBALF-GET-NEXT}); {@link #findAllByOrderByAcctIdAscTypeCdAscCatCdAsc()} reproduces
 * that ordered scan.
 */
@Repository
public interface TranCatBalanceRepository
        extends JpaRepository<TranCatBalance, TranCatBalance.TranCatBalanceId> {

    /** Sequential read of TCATBALF in primary-key order (acct id, type, category). */
    List<TranCatBalance> findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();
}
