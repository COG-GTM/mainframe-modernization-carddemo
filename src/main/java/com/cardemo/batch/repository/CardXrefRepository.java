package com.cardemo.batch.repository;

import com.cardemo.batch.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    Optional<CardXref> findByAcctId(long acctId);
}
