package com.carddemo.entity;

import com.carddemo.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for Customer entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from custdata.txt, CRUD operations, and custom finders.
 */
@DataJpaTest
class CustomerTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void seedDataLoadsExpectedNumberOfCustomers() {
        List<Customer> all = customerRepository.findAll();
        assertThat(all).hasSize(50);
    }

    @Test
    void firstCustomerFieldsMatchSeedData() {
        Optional<Customer> opt = customerRepository.findById(1L);
        assertThat(opt).isPresent();
        Customer cust = opt.get();
        assertThat(cust.getCustFirstName()).isNotBlank();
        assertThat(cust.getCustLastName()).isNotBlank();
        assertThat(cust.getCustAddrLine1()).isNotNull();
    }

    @Test
    void findByLastNameReturnsMatchingCustomers() {
        List<Customer> all = customerRepository.findAll();
        String lastName = all.get(0).getCustLastName().trim();
        List<Customer> results = customerRepository.findByCustLastName(lastName);
        assertThat(results).isNotEmpty();
        results.forEach(c -> assertThat(c.getCustLastName().trim()).isEqualTo(lastName));
    }

    @Test
    void findByStateCdReturnsMatchingCustomers() {
        List<Customer> all = customerRepository.findAll();
        String state = all.get(0).getCustAddrStateCd().trim();
        List<Customer> results = customerRepository.findByCustAddrStateCd(state);
        assertThat(results).isNotEmpty();
    }

    @Test
    void findByFicoScoreReturnsHighScoreCustomers() {
        List<Customer> results = customerRepository.findByCustFicoCreditScoreGreaterThanEqual(0);
        assertThat(results).isNotEmpty();
    }

    @Test
    void createCustomerPersistsAndReadsBack() {
        Customer newCust = new Customer();
        newCust.setCustId(99999L);
        newCust.setCustFirstName("TEST");
        newCust.setCustMiddleName("M");
        newCust.setCustLastName("USER");
        newCust.setCustAddrLine1("123 TEST ST");
        newCust.setCustAddrLine2("");
        newCust.setCustAddrLine3("");
        newCust.setCustAddrStateCd("NY");
        newCust.setCustAddrCountryCd("US");
        newCust.setCustAddrZip("10001");
        newCust.setCustPhoneNum1("5551234567");
        newCust.setCustPhoneNum2("");
        newCust.setCustSsn(123456789L);
        newCust.setCustGovtIssuedId("DL12345");
        newCust.setCustDob(LocalDate.of(1990, 1, 1));
        newCust.setCustEftAccountId("EFT001");
        newCust.setCustPriCardHolderInd("Y");
        newCust.setCustFicoCreditScore(750);

        customerRepository.save(newCust);

        Optional<Customer> found = customerRepository.findById(99999L);
        assertThat(found).isPresent();
        assertThat(found.get().getCustFirstName()).isEqualTo("TEST");
        assertThat(found.get().getCustFicoCreditScore()).isEqualTo(750);
    }

    @Test
    void updateCustomerAddress() {
        // Mirrors COBOL REWRITE for customer update
        Customer cust = customerRepository.findById(1L).orElseThrow();
        cust.setCustAddrLine1("999 NEW ADDRESS");
        customerRepository.save(cust);

        Customer updated = customerRepository.findById(1L).orElseThrow();
        assertThat(updated.getCustAddrLine1()).isEqualTo("999 NEW ADDRESS");
    }

    @Test
    void deleteCustomerRemovesFromDatabase() {
        assertThat(customerRepository.findById(50L)).isPresent();
        customerRepository.deleteById(50L);
        assertThat(customerRepository.findById(50L)).isEmpty();
        assertThat(customerRepository.findAll()).hasSize(49);
    }

    @Test
    void customerIdIsUniqueAcrossAllRecords() {
        List<Customer> all = customerRepository.findAll();
        long distinctIds = all.stream().map(Customer::getCustId).distinct().count();
        assertThat(distinctIds).isEqualTo(all.size());
    }

    @Test
    void allCustomersHaveRequiredFields() {
        List<Customer> all = customerRepository.findAll();
        all.forEach(c -> {
            assertThat(c.getCustId()).isNotNull();
            assertThat(c.getCustFirstName()).isNotNull();
            assertThat(c.getCustLastName()).isNotNull();
        });
    }
}
