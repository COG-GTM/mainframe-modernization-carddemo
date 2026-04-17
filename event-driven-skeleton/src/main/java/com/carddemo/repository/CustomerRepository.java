package com.carddemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.model.Customer;

/**
 * Repository for customer master data.
 *
 * Replaces: CUSTFILE READ by key (FD-CUST-ID) in CBSTM03A
 * (2000-CUSTFILE-GET via CBSTM03B READ-K operation).
 *
 * Used for statement generation: customer name, address, and
 * FICO credit score appear in statement headers.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
