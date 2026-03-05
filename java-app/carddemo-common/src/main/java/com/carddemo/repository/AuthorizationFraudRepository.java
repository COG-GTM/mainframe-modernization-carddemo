package com.carddemo.repository;

import com.carddemo.entity.AuthorizationFraud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AuthorizationFraud entity.
 * Replaces DB2 INSERT/SELECT on AUTHFRDS table.
 */
@Repository
public interface AuthorizationFraudRepository extends JpaRepository<AuthorizationFraud, Long> {
    List<AuthorizationFraud> findByCardNum(String cardNum);
}
