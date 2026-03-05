package com.carddemo.repository;

import com.carddemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer entity.
 * Replaces CICS READ on CUSTDAT VSAM file.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
