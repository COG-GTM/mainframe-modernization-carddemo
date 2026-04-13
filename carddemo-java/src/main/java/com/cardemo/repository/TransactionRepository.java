package com.cardemo.repository;

import com.cardemo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Transaction}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ (STARTBR/READNEXT/READPREV), WRITE, REWRITE.
 * Programs: COTRN00C (list), COTRN01C (view), COTRN02C (add),
 *           COBIL00C (billing), CORPT00C (report), CBTRN01C/02C/03C (batch).
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranCardNum(String cardNum);

    List<Transaction> findByTranTypeCd(String typeCd);

    List<Transaction> findByTranCardNumAndTranTypeCd(String cardNum, String typeCd);
}
