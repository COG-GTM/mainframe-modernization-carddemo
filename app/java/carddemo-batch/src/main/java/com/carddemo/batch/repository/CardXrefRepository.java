package com.carddemo.batch.repository;

import com.carddemo.batch.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
}
