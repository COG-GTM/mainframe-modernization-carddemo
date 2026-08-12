package com.carddemo.repository;

import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.model.entity.TransactionCategoryBalanceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS TCATBALF access (keyed on TRAN-CAT-KEY). */
@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findByIdAccountIdOrderByIdTypeCodeAscIdCategoryCodeAsc(Long accountId);
}
