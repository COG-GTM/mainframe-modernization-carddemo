package com.cardemo.repository;

import com.cardemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link CardXref}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ, WRITE, REWRITE.
 * Programs: COCRDSLC (select card), COCRDUPC (update card), COACTUPC (account update),
 *           CBACT03C (batch list), CBTRN03C (batch xref).
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefCustId(Long custId);

    List<CardXref> findByXrefAcctId(Long acctId);
}
