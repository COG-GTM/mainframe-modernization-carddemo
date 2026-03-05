package com.carddemo.repository;

import com.carddemo.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TransactionType entity.
 * Replaces DB2 cursor paging on TRTYP table.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
