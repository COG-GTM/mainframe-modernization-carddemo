package com.carddemo.accountservice.repository;

import com.carddemo.accountservice.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findFirstByXrefAcctId(Long xrefAcctId);
}
