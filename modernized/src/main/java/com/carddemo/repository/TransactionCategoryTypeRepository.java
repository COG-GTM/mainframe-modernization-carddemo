package com.carddemo.repository;

import com.carddemo.domain.TransactionCategoryType;
import com.carddemo.domain.TransactionCategoryTypeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryTypeRepository
        extends JpaRepository<TransactionCategoryType, TransactionCategoryTypeId> {
}
