package com.carddemo.repository;

import com.carddemo.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS CUSTDATA access (keyed on CUST-ID). */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
