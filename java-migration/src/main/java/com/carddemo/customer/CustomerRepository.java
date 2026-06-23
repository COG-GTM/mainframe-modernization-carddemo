package com.carddemo.customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Customer}.
 *
 * <p>Mirrors the COBOL/VSAM access patterns for CUSTDAT:</p>
 * <ul>
 *   <li>Keyed READ by {@code CUST-ID} — {@link #findById} (see {@code COACTUPC.cbl}).</li>
 *   <li>Sequential READ of the whole file — {@link #findAll} (see {@code CBCUS01C.cbl}).</li>
 *   <li>Secondary lookups by last name / FICO score — finder methods below.</li>
 * </ul>
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Customers with the given last name, mirroring CUST-LAST-NAME scans. */
    List<Customer> findByCustLastName(String custLastName);

    /** Customers with the given FICO credit score, mirroring CUST-FICO-CREDIT-SCORE filters. */
    List<Customer> findByCustFicoCreditScore(Integer custFicoCreditScore);
}
