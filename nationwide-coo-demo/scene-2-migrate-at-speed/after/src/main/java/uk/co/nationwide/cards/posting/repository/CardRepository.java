package uk.co.nationwide.cards.posting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.nationwide.cards.posting.domain.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
}
