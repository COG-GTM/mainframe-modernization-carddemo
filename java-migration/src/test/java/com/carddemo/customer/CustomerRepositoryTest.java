package com.carddemo.customer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Verifies the Customer entity mapping, Flyway seed data, and repository finders
 * against an H2 (PostgreSQL mode) database loaded by the real Flyway migrations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    void seedDataLoadsExpectedRecordCount() {
        assertThat(repository.count()).isEqualTo(50);
    }

    @Test
    void firstSeedRecordFieldsMatchParsedValues() {
        Customer cust = repository.findById(1L).orElseThrow();
        assertThat(cust.getCustFirstName()).isEqualTo("Immanuel");
        assertThat(cust.getCustMiddleName()).isEqualTo("Madeline");
        assertThat(cust.getCustLastName()).isEqualTo("Kessler");
        assertThat(cust.getCustAddrLine1()).isEqualTo("618 Deshaun Route");
        assertThat(cust.getCustAddrStateCd()).isEqualTo("NC");
        assertThat(cust.getCustAddrCountryCd()).isEqualTo("USA");
        assertThat(cust.getCustAddrZip()).isEqualTo("12546");
        assertThat(cust.getCustPhoneNum1()).isEqualTo("(908)119-8310");
        assertThat(cust.getCustGovtIssuedId()).isEqualTo("00000000000049368437");
        assertThat(cust.getCustDobYyyyMmDd()).isEqualTo(LocalDate.of(1961, 6, 8));
        assertThat(cust.getCustEftAccountId()).isEqualTo("0053581756");
        assertThat(cust.getCustPriCardHolderInd()).isEqualTo("Y");
        assertThat(cust.getCustFicoCreditScore()).isEqualTo(274);
    }

    @Test
    void ssnPreservesLeadingZeros() {
        Customer cust = repository.findById(1L).orElseThrow();
        assertThat(cust.getCustSsn()).isEqualTo("020973888");
    }

    @Test
    void findByIdReturnsEmptyForUnknownKey() {
        assertThat(repository.findById(999999999L)).isEmpty();
    }

    @Test
    void findAllReturnsAllSeededCustomers() {
        assertThat(repository.findAll()).hasSize(50);
    }

    @Test
    void findByCustLastNameReturnsMatchingCustomers() {
        assertThat(repository.findByCustLastName("Kessler"))
                .extracting(Customer::getCustId)
                .contains(1L);
        assertThat(repository.findByCustLastName("NoSuchName")).isEmpty();
    }

    @Test
    void findByCustFicoCreditScoreReturnsMatchingCustomers() {
        List<Customer> matches = repository.findByCustFicoCreditScore(274);
        assertThat(matches).isNotEmpty();
        assertThat(matches).allMatch(c -> c.getCustFicoCreditScore() == 274);
        assertThat(repository.findByCustFicoCreditScore(-1)).isEmpty();
    }

    @Test
    void createPersistsNewCustomer() {
        Customer cust = newCustomer(900000001L, "Smith", 720);
        repository.save(cust);

        Optional<Customer> reloaded = repository.findById(900000001L);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCustLastName()).isEqualTo("Smith");
        assertThat(reloaded.get().getCustSsn()).isEqualTo("000123456");
    }

    @Test
    void updateModifiesExistingCustomer() {
        Customer cust = repository.findById(2L).orElseThrow();
        cust.setCustFicoCreditScore(800);
        repository.saveAndFlush(cust);

        assertThat(repository.findById(2L).orElseThrow().getCustFicoCreditScore())
                .isEqualTo(800);
    }

    @Test
    void deleteRemovesCustomer() {
        repository.deleteById(3L);
        assertThat(repository.findById(3L)).isEmpty();
        assertThat(repository.count()).isEqualTo(49);
    }

    private static Customer newCustomer(Long id, String lastName, Integer fico) {
        Customer cust = new Customer();
        cust.setCustId(id);
        cust.setCustFirstName("Test");
        cust.setCustMiddleName("Q");
        cust.setCustLastName(lastName);
        cust.setCustAddrLine1("1 Main St");
        cust.setCustAddrStateCd("CA");
        cust.setCustAddrCountryCd("USA");
        cust.setCustAddrZip("90001");
        cust.setCustPhoneNum1("(555)555-5555");
        cust.setCustSsn("000123456");
        cust.setCustGovtIssuedId("ID-0001");
        cust.setCustDobYyyyMmDd(LocalDate.of(1990, 1, 1));
        cust.setCustEftAccountId("0000000001");
        cust.setCustPriCardHolderInd("Y");
        cust.setCustFicoCreditScore(fico);
        return cust;
    }
}
