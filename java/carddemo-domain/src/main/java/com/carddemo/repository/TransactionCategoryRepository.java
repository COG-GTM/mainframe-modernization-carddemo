package com.carddemo.repository;

import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, TransactionCategoryId> {
}
