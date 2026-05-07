package com.carddemo.service;

import com.carddemo.entity.Customer;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
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

    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByLastName(String lastName) {
        return customerRepository.findByLastName(lastName);
    }

    @Transactional(readOnly = true)
    public List<Customer> findByStateCode(String stateCode) {
        return customerRepository.findByStateCode(stateCode);
    }
}
