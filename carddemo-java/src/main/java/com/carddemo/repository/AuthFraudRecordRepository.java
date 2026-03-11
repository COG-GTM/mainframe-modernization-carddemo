package com.carddemo.repository;

import com.carddemo.entity.AuthFraudRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuthFraudRecordRepository extends JpaRepository<AuthFraudRecord, Long> {
    List<AuthFraudRecord> findByCardNum(String cardNum);
}
