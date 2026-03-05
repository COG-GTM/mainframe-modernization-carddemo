package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardCrossReference;
import com.carddemo.entity.CardData;
import com.carddemo.entity.Customer;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.DailyTransactionReject;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionType;
import com.carddemo.entity.User;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardCrossReferenceRepository;
import com.carddemo.repository.CardDataRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRejectRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies all entities can be persisted and retrieved using the H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class EntityMappingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardDataRepository cardDataRepository;

    @Autowired
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private DailyTransactionRejectRepository dailyTransactionRejectRepository;

    @Test
    void shouldPersistAndRetrieveUser() {
        User user = User.builder()
                .userId("TESTUSER")
                .firstName("Test")
                .lastName("User")
                .password("PASS1234")
                .userType("U")
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findById("TESTUSER");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getLastName()).isEqualTo("User");
        assertThat(found.get().getUserType()).isEqualTo("U");
    }

    @Test
    void shouldPersistAndRetrieveAccount() {
        Account account = Account.builder()
                .accountId(99999999901L)
                .accountStatus("Y")
                .currentBalance(new BigDecimal("1000.50"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .openDate("2020-01-01")
                .expirationDate("2025-12-31")
                .reissueDate("2023-01-01")
                .currentCycleCredit(new BigDecimal("200.00"))
                .currentCycleDebit(new BigDecimal("100.00"))
                .addressZip("10001")
                .groupId("GRP001")
                .build();

        accountRepository.save(account);

        Optional<Account> found = accountRepository.findById(99999999901L);
        assertThat(found).isPresent();
        assertThat(found.get().getAccountStatus()).isEqualTo("Y");
        assertThat(found.get().getCurrentBalance()).isEqualByComparingTo(new BigDecimal("1000.50"));
    }

    @Test
    void shouldPersistAndRetrieveCustomer() {
        Customer customer = Customer.builder()
                .customerId(999000001L)
                .firstName("Test")
                .middleName("M")
                .lastName("Customer")
                .addressLine1("123 Test St")
                .addressLine2("")
                .addressLine3("")
                .stateCode("TX")
                .countryCode("US")
                .zipCode("75001")
                .phoneNumber1("214-555-0100")
                .phoneNumber2("")
                .ssn("111223333")
                .govtIssuedId("TX1234567")
                .dateOfBirth("1980-01-01")
                .eftAccountId("9876543210")
                .priCardHolderInd("Y")
                .ficoScore(720)
                .build();

        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findById(999000001L);
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getFicoScore()).isEqualTo(720);
    }

    @Test
    void shouldPersistAndRetrieveCardData() {
        Account account = Account.builder()
                .accountId(99999999902L)
                .accountStatus("Y")
                .currentBalance(BigDecimal.ZERO)
                .creditLimit(new BigDecimal("3000.00"))
                .build();
        accountRepository.save(account);

        CardData card = CardData.builder()
                .cardNumber("5555000011112222")
                .account(account)
                .cvvCode(789)
                .embossedName("TEST CARDHOLDER")
                .expirationDate("2026-12-31")
                .cardStatus("Y")
                .build();

        cardDataRepository.save(card);

        Optional<CardData> found = cardDataRepository.findById("5555000011112222");
        assertThat(found).isPresent();
        assertThat(found.get().getEmbossedName()).isEqualTo("TEST CARDHOLDER");
        assertThat(found.get().getCvvCode()).isEqualTo(789);
    }

    @Test
    void shouldPersistAndRetrieveCardCrossReference() {
        Account account = Account.builder()
                .accountId(99999999903L)
                .accountStatus("Y")
                .currentBalance(BigDecimal.ZERO)
                .build();
        accountRepository.save(account);

        Customer customer = Customer.builder()
                .customerId(999000002L)
                .firstName("Xref")
                .lastName("Test")
                .build();
        customerRepository.save(customer);

        CardCrossReference xref = CardCrossReference.builder()
                .cardNumber("6666000033334444")
                .account(account)
                .customer(customer)
                .build();

        cardCrossReferenceRepository.save(xref);

        Optional<CardCrossReference> found = cardCrossReferenceRepository.findById("6666000033334444");
        assertThat(found).isPresent();
    }

    @Test
    void shouldPersistAndRetrieveTransaction() {
        Transaction transaction = Transaction.builder()
                .transactionId("TESTTXN000000001")
                .transactionType("PU")
                .transactionCategory(5411)
                .transactionSource("POS")
                .transactionDescription("Test Purchase")
                .transactionAmount(new BigDecimal("99.99"))
                .merchantId("111222333")
                .merchantName("Test Merchant")
                .merchantCity("Test City")
                .merchantZip("12345")
                .cardNumber("5555000011112222")
                .originTimestamp("2024-01-01 12:00:00.000000")
                .processingTimestamp("2024-01-01 12:00:01.000000")
                .build();

        transactionRepository.save(transaction);

        Optional<Transaction> found = transactionRepository.findById("TESTTXN000000001");
        assertThat(found).isPresent();
        assertThat(found.get().getTransactionAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldPersistAndRetrieveTransactionType() {
        TransactionType type = TransactionType.builder()
                .typeCode("TS")
                .typeDescription("Test Type")
                .build();

        transactionTypeRepository.save(type);

        Optional<TransactionType> found = transactionTypeRepository.findById("TS");
        assertThat(found).isPresent();
        assertThat(found.get().getTypeDescription()).isEqualTo("Test Type");
    }

    @Test
    void shouldPersistAndRetrieveDailyTransaction() {
        DailyTransaction daily = DailyTransaction.builder()
                .transactionId("DTXN000000000001")
                .transactionType("PU")
                .transactionCategory(5999)
                .transactionSource("BATCH")
                .transactionDescription("Daily Batch Purchase")
                .transactionAmount(new BigDecimal("150.00"))
                .merchantId("444555666")
                .merchantName("Daily Merchant")
                .merchantCity("Batch City")
                .merchantZip("54321")
                .cardNumber("5555000011112222")
                .originTimestamp("2024-02-01 08:00:00.000000")
                .build();

        DailyTransaction saved = dailyTransactionRepository.save(daily);

        assertThat(saved.getId()).isNotNull();
        Optional<DailyTransaction> found = dailyTransactionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTransactionDescription()).isEqualTo("Daily Batch Purchase");
    }

    @Test
    void shouldPersistAndRetrieveDailyTransactionReject() {
        DailyTransactionReject reject = DailyTransactionReject.builder()
                .transactionId("RTXN000000000001")
                .transactionType("PU")
                .transactionCategory(5999)
                .transactionSource("BATCH")
                .transactionDescription("Rejected Transaction")
                .transactionAmount(new BigDecimal("999.99"))
                .cardNumber("0000000000000000")
                .rejectReasonCode(1)
                .rejectReasonDescription("Invalid card number")
                .build();

        DailyTransactionReject saved = dailyTransactionRejectRepository.save(reject);

        assertThat(saved.getId()).isNotNull();
        Optional<DailyTransactionReject> found = dailyTransactionRejectRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRejectReasonCode()).isEqualTo(1);
        assertThat(found.get().getRejectReasonDescription()).isEqualTo("Invalid card number");
    }
}
