package com.cardemo.repository;

import com.cardemo.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link TransactionType}.
 * Reference data: maps type codes to descriptions.
 * Programs: COTRN02C (add transaction — lookup type).
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
