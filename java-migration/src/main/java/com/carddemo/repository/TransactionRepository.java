package com.carddemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.entity.Transaction;

/**
 * Spring Data JPA repository for {@link Transaction} entities.
 * <p>
 * Mirrors COBOL VSAM access patterns:
 * <ul>
 *   <li>STARTBR/READNEXT by card number (COTRN00C)</li>
 *   <li>READ by transaction ID (COTRN01C)</li>
 *   <li>Lookup by merchant (COBIL00C billing)</li>
 * </ul>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCardNum(String cardNum);

    List<Transaction> findByTypeCode(String typeCode);

    List<Transaction> findByMerchantId(Long merchantId);
}
