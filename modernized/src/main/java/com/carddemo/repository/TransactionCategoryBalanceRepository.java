package com.carddemo.repository;

import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findByIdAccountIdOrderByIdTypeCodeAscIdCategoryCodeAsc(Long accountId);

    List<TransactionCategoryBalance> findAllByOrderByIdAccountIdAscIdTypeCodeAscIdCategoryCodeAsc();
}
