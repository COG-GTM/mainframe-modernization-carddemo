package com.cardemo.repository;

import com.cardemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link DailyTransaction}.
 * Mirrors COBOL access patterns: sequential READ, WRITE, REWRITE.
 * Programs: CBTRN01C (batch validate), CBTRN02C (batch post).
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {

    List<DailyTransaction> findByDalytranCardNum(String cardNum);

    List<DailyTransaction> findByDalytranTypeCd(String typeCd);
}
