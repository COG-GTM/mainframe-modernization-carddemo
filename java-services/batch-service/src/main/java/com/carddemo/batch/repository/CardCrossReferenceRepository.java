package com.carddemo.batch.repository;

import com.carddemo.batch.model.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {

    Optional<CardCrossReference> findByCardNum(String cardNum);
}
