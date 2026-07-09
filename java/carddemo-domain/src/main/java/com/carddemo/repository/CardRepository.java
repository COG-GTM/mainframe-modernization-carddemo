package com.carddemo.repository;

import com.carddemo.domain.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByCardAcctId(String cardAcctId);
}
