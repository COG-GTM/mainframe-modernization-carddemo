package com.carddemo.repository;

import com.carddemo.entity.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {

    List<CardCrossReference> findByAccountId(Long accountId);

    List<CardCrossReference> findByCustomerId(Long customerId);
}
