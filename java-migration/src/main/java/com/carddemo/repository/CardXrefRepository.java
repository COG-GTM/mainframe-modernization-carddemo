package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link CardXref} entities.
 * <p>
 * Provides CRUD operations and custom finders that replace
 * CICS file-control commands (READ, WRITE, REWRITE, STARTBR, READNEXT)
 * used in COBOL programs such as COTRN02C, COBIL00C, COACTVWC, etc.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByCustomerId(Long customerId);

    List<CardXref> findByAccountId(Long accountId);
}
