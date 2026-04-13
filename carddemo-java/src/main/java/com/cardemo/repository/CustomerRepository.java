package com.cardemo.repository;

import com.cardemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Customer}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ, WRITE, REWRITE.
 * Programs: COACTUPC (account update), COACTVWC (account view),
 *           CBCUS01C (batch list), CBSTM03A/B (statements), CBTRN01C (transactions).
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCustLastName(String lastName);

    List<Customer> findByCustAddrStateCd(String stateCd);

    List<Customer> findByCustAddrZip(String zip);
}
