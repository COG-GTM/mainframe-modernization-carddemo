package com.cardemo.batch.repository;

import com.cardemo.batch.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for XREFFILE — card cross-references.
 * Replaces CBSTM03B sequential reads on XREF-FILE.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findAllByOrderByCardNumAsc();
}
