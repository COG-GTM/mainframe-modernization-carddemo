package com.cardemo.repository;

import com.cardemo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Account}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ, REWRITE, WRITE.
 * Programs: COACTUPC (online update), COACTVWC (online view), COBIL00C (billing),
 *           CBACT01C/CBACT04C (batch), CBSTM03A/B (statements), CBTRN01C/02C (transactions).
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByAcctActiveStatus(String activeStatus);

    List<Account> findByAcctGroupId(String groupId);

    List<Account> findByAcctAddrZip(String zip);
}
