package com.cardemo.batch.repository;

import com.cardemo.batch.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CUSTFILE — customer records.
 * Replaces CBSTM03B keyed reads (READ-K) on CUST-FILE.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
}
