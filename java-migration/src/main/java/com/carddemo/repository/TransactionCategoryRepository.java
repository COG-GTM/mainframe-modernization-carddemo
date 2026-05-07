package com.carddemo.repository;

import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TransactionCategory entity.
 *
 * <p>Provides CRUD operations and custom finders for transaction categories
 * migrated from the COBOL TRANCATG VSAM file.
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {

    /**
     * Find all transaction categories by type code.
     *
     * @param typeCode the transaction type code (e.g., "01", "02")
     * @return list of categories matching the type code
     */
    List<TransactionCategory> findByIdTypeCode(String typeCode);

    /**
     * Find transaction categories whose description contains the given text (case-insensitive).
     *
     * @param keyword the search keyword
     * @return list of categories with matching descriptions
     */
    List<TransactionCategory> findByDescriptionContainingIgnoreCase(String keyword);
}
