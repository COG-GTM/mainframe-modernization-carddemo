package com.carddemo.repository;

import com.carddemo.domain.Customer;

import java.util.List;
import java.util.Optional;

/** CUSTDATA (VSAM KSDS keyed on CUST-ID). */
public interface CustomerRepository {

    Optional<Customer> findById(String customerId);

    List<Customer> findAll();

    Customer save(Customer customer);
}
