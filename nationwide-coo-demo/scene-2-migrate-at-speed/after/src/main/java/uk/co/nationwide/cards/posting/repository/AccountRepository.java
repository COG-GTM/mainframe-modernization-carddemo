package uk.co.nationwide.cards.posting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.nationwide.cards.posting.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
