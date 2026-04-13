package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranCardNum(String cardNum);

    List<Transaction> findByTranTypeCd(String typeCd);

    List<Transaction> findByTranMerchantId(Long merchantId);
}
