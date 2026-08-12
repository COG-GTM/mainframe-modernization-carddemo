package com.carddemo.repository;

import com.carddemo.model.entity.TransactionCategory;
import com.carddemo.model.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces TRANCATG file access. */
@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, TransactionCategoryId> {
}
