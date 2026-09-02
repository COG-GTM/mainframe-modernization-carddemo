package com.carddemo.repository;

import com.carddemo.domain.TransactionType;

import java.util.List;
import java.util.Optional;

/** TRANTYPE reference table. */
public interface TransactionTypeRepository {

    Optional<TransactionType> findByCode(String typeCode);

    List<TransactionType> findAll();
}
