package com.carddemo.repository;

import com.carddemo.domain.Card;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccountIdOrderByCardNumber(Long accountId);

    Page<Card> findByAccountId(Long accountId, Pageable pageable);
}
