package com.carddemo.repository;

import com.carddemo.domain.TransactionCategory;

import java.util.List;
import java.util.Optional;

/** TRANCATG reference table. */
public interface TransactionCategoryRepository {

    Optional<TransactionCategory> find(String typeCode, int categoryCode);

    List<TransactionCategory> findAll();
}
