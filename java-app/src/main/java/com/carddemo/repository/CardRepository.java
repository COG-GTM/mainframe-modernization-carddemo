package com.carddemo.repository;

import com.carddemo.domain.Card;

import java.util.List;
import java.util.Optional;

/** CARDDATA (VSAM KSDS keyed on CARD-NUM, alternate index on CARD-ACCT-ID). */
public interface CardRepository {

    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByAccountId(String accountId);

    List<Card> findAll();

    Card save(Card card);
}
