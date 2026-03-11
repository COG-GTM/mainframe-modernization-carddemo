package com.carddemo.repository;

import com.carddemo.entity.CardAccountXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardAccountXrefRepository extends JpaRepository<CardAccountXref, String> {
    Optional<CardAccountXref> findByCardNum(String cardNum);
}
