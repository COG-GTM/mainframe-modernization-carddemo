package com.carddemo.repository;

import com.carddemo.entity.TransactionTypeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionTypeCategoryRepository extends JpaRepository<TransactionTypeCategory, TransactionTypeCategory.TrcKey> {
    List<TransactionTypeCategory> findByTrcTypeCode(String typeCode);
}
