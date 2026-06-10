package com.carddemo.repository;

import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    Optional<TranCatBalance> findByIdAcctIdAndIdTypeCdAndIdCatCd(long acctId, String typeCd, int catCd);

    default Optional<TranCatBalance> findByAcctIdAndTypeCdAndCatCd(long acctId, String typeCd, int catCd) {
        return findByIdAcctIdAndIdTypeCdAndIdCatCd(acctId, typeCd, catCd);
    }
}
