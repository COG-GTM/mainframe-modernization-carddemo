package com.carddemo.repository;

import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategory.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TransactionCategory entity.
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {
}
