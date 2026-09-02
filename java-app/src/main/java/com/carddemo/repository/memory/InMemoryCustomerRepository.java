package com.carddemo.repository.memory;

import com.carddemo.domain.Customer;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CUSTDATA backed by a map seeded from custdata.txt. */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, Customer> customers = new LinkedHashMap<>();

    public InMemoryCustomerRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        customers.clear();
        dataSet.customers()
                .forEach(customer -> customers.put(customer.getCustomerId(), Customer.parse(customer.format())));
    }

    @Override
    public Optional<Customer> findById(String customerId) {
        return Optional.ofNullable(customers.get(RecordKeys.customerId(customerId)));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public Customer save(Customer customer) {
        customers.put(RecordKeys.customerId(customer.getCustomerId()), customer);
        return customer;
    }
}
