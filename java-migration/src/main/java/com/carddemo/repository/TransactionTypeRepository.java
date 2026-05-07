package com.carddemo.repository;

import com.carddemo.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TransactionType} entities.
 *
 * <p>Provides CRUD operations and custom finders for transaction types
 * migrated from the COBOL TRANTYPE VSAM file.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {

    /**
     * Finds transaction types whose description contains the given substring
     * (case-sensitive).
     *
     * @param description substring to search for
     * @return list of matching transaction types
     */
    List<TransactionType> findByDescriptionContaining(String description);
}
