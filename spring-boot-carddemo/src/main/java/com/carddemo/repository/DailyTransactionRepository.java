package com.carddemo.repository;

import com.carddemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {

    List<DailyTransaction> findByDalytranCardNum(String cardNum);

    List<DailyTransaction> findByDalytranTypeCd(String typeCd);
}
