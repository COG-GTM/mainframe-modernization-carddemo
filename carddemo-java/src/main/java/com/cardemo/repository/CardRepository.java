package com.cardemo.repository;

import com.cardemo.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Card}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ (STARTBR/READNEXT/READPREV), REWRITE, WRITE.
 * Programs: COCRDLIC (list), COCRDSLC (select), COCRDUPC (update), COACTUPC (account update).
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByCardAcctId(Long acctId);

    List<Card> findByCardActiveStatus(String activeStatus);

    List<Card> findByCardAcctIdAndCardActiveStatus(Long acctId, String activeStatus);
}
