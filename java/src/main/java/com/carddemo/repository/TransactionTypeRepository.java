package com.carddemo.repository;

import com.carddemo.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces TRANTYPE file access. */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
