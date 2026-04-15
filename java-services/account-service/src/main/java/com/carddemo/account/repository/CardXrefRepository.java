package com.carddemo.account.repository;

import com.carddemo.account.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    Optional<CardXref> findByCardNumber(String cardNumber);
}
