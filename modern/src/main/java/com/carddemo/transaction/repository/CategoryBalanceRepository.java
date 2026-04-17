package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.CategoryBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for transaction category balance data.
 *
 * COBOL Traceability: Replaces READ/REWRITE on TCATBALF VSAM file
 * used by CBTRN02C (posting) and CBACT04C (interest calculation).
 */
@Repository
public interface CategoryBalanceRepository
        extends JpaRepository<CategoryBalanceEntity, CategoryBalanceEntity.CategoryBalanceId> {

    List<CategoryBalanceEntity> findByAccountId(String accountId);
}
