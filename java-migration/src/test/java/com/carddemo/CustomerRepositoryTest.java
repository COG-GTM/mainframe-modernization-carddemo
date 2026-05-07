package com.carddemo;

import com.carddemo.entity.Customer;
import com.carddemo.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldLoadAll50SeededRecords() {
        List<Customer> customers = customerRepository.findAll();
        assertThat(customers).hasSize(50);
    }

    @Test
    void shouldFindFirstRecordWithCorrectValues() {
        Optional<Customer> opt = customerRepository.findById(1L);
        assertThat(opt).isPresent();

        Customer c = opt.get();
        assertThat(c.getFirstName()).isEqualTo("Immanuel");
        assertThat(c.getMiddleName()).isEqualTo("Madeline");
        assertThat(c.getLastName()).isEqualTo("Kessler");
        assertThat(c.getAddressLine1()).isEqualTo("618 Deshaun Route");
        assertThat(c.getStateCode()).isEqualTo("NC");
        assertThat(c.getCountryCode()).isEqualTo("USA");
        assertThat(c.getDateOfBirth()).isEqualTo(LocalDate.of(1961, 6, 8));
        assertThat(c.getPrimaryCardHolderIndicator()).isEqualTo("Y");
        assertThat(c.getFicoCreditScore()).isEqualTo(274);
    }

    @Test
    void shouldFindLastRecordById() {
        Optional<Customer> opt = customerRepository.findById(50L);
        assertThat(opt).isPresent();

        Customer c = opt.get();
        assertThat(c.getFirstName()).isEqualTo("Aniya");
        assertThat(c.getLastName()).isEqualTo("Von");
        assertThat(c.getStateCode()).isEqualTo("OR");
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Customer> opt = customerRepository.findById(9999L);
        assertThat(opt).isEmpty();
    }

    @Test
    void shouldFindByLastName() {
        List<Customer> results = customerRepository.findByLastName("Mann");
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(c -> c.getLastName().equals("Mann"));
    }

    @Test
    void shouldFindByStateCode() {
        List<Customer> results = customerRepository.findByStateCode("MI");
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).allMatch(c -> c.getStateCode().equals("MI"));
    }

    @Test
    void shouldFindBySsn() {
        List<Customer> results = customerRepository.findBySsn(20973888L);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void shouldCreateAndDeleteCustomer() {
        Customer newCustomer = Customer.builder()
                .id(9999L)
                .firstName("Test")
                .middleName("Middle")
                .lastName("User")
                .addressLine1("123 Test Street")
                .stateCode("TX")
                .countryCode("USA")
                .zipCode("75001")
                .ssn(123456789L)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .primaryCardHolderIndicator("Y")
                .ficoCreditScore(750)
                .build();

        Customer saved = customerRepository.save(newCustomer);
        assertThat(saved.getId()).isEqualTo(9999L);
        assertThat(customerRepository.findAll()).hasSize(51);

        customerRepository.deleteById(9999L);
        assertThat(customerRepository.findAll()).hasSize(50);
        assertThat(customerRepository.findById(9999L)).isEmpty();
    }

    @Test
    void shouldUpdateExistingCustomer() {
        Optional<Customer> opt = customerRepository.findById(1L);
        assertThat(opt).isPresent();

        Customer customer = opt.get();
        customer.setFicoCreditScore(800);
        customer.setAddressLine1("999 Updated Avenue");
        customerRepository.save(customer);

        Customer updated = customerRepository.findById(1L).orElseThrow();
        assertThat(updated.getFicoCreditScore()).isEqualTo(800);
        assertThat(updated.getAddressLine1()).isEqualTo("999 Updated Avenue");
    }

    @Test
    void shouldHandleCustomerWithSingleQuoteInName() {
        List<Customer> results = customerRepository.findByLastName("O'Connell");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(31L);
        assertThat(results.get(0).getFirstName()).isEqualTo("Lucious");
    }
}
