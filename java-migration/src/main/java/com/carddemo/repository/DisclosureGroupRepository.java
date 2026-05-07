package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link DisclosureGroup} entities.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    /**
     * Find all disclosure groups by account group identifier.
     * COBOL equivalent: keyed read on DIS-ACCT-GROUP-ID.
     */
    List<DisclosureGroup> findByIdAccountGroupId(String accountGroupId);

    /**
     * Find all disclosure groups by transaction type code.
     * COBOL equivalent: keyed read on DIS-TRAN-TYPE-CD.
     */
    List<DisclosureGroup> findByIdTransactionTypeCode(String transactionTypeCode);
}
