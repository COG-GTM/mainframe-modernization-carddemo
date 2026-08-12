package com.carddemo.repository;

import com.carddemo.model.entity.Card;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS CARDDATA access (keyed on CARD-NUM, AIX on CARD-ACCT-ID). */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccountIdOrderByCardNumber(Long accountId);

    Page<Card> findByAccountId(Long accountId, Pageable pageable);

    Optional<Card> findByCardNumberAndAccountId(String cardNumber, Long accountId);
}
