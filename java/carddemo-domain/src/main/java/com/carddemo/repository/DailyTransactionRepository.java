package com.carddemo.repository;

import com.carddemo.domain.DailyTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {

    List<DailyTransaction> findByTranCardNum(String tranCardNum);
}
