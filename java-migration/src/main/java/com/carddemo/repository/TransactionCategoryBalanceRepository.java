package com.carddemo.repository;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TransactionCategoryBalance}.
 * <p>
 * Provides CRUD operations and custom finders replacing COBOL VSAM file
 * access patterns (CBACT04C sequential reads, CBTRN02C random key lookups).
 */
@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    /**
     * Find all balances for a given account.
     * Replaces COBOL pattern: sequential read of TCATBAL-FILE filtered by TRANCAT-ACCT-ID.
     *
     * @param accountId the account identifier
     * @return list of transaction category balances for the account
     */
    List<TransactionCategoryBalance> findByIdAccountId(Long accountId);

    /**
     * Find all balances for a given transaction type code.
     * Replaces COBOL pattern: browse by TRANCAT-TYPE-CD.
     *
     * @param typeCode the transaction type code (2 characters)
     * @return list of transaction category balances for the type
     */
    List<TransactionCategoryBalance> findByIdTypeCode(String typeCode);
}
