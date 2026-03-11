package com.carddemo.repository;

import com.carddemo.entity.AuthSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuthSummaryRepository extends JpaRepository<AuthSummary, Long> {
    List<AuthSummary> findByCardNum(String cardNum);
}
