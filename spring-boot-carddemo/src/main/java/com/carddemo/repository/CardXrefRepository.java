package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefCustId(Long custId);

    List<CardXref> findByXrefAcctId(Long acctId);
}
