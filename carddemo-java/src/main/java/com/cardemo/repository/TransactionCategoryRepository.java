package com.cardemo.repository;

import com.cardemo.entity.TransactionCategory;
import com.cardemo.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TransactionCategory}.
 * Reference data: maps type + category codes to descriptions.
 * Programs: COTRN02C (add transaction — lookup category).
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {

    List<TransactionCategory> findByTranTypeCd(String typeCd);
}
