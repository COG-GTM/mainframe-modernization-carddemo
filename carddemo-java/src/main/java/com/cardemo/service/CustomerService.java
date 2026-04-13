package com.cardemo.service;

import com.cardemo.entity.Customer;
import com.cardemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Customer entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ, WRITE, REWRITE.
 */
@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long custId) {
        return customerRepository.findById(custId);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer update(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByLastName(String lastName) {
        return customerRepository.findByCustLastName(lastName);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByStateCd(String stateCd) {
        return customerRepository.findByCustAddrStateCd(stateCd);
    }
}
