package com.carddemo;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardCrossReference;
import com.carddemo.entity.CardData;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.User;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardCrossReferenceRepository;
import com.carddemo.repository.CardDataRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests custom query methods in each repository.
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

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

    private Account testAccount1;
    private Account testAccount2;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create test accounts
        testAccount1 = Account.builder()
                .accountId(11111111111L)
                .accountStatus("Y")
                .currentBalance(new BigDecimal("1000.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .build();
        accountRepository.save(testAccount1);

        testAccount2 = Account.builder()
                .accountId(22222222222L)
                .accountStatus("Y")
                .currentBalance(new BigDecimal("2000.00"))
                .creditLimit(new BigDecimal("10000.00"))
                .build();
        accountRepository.save(testAccount2);

        // Create test customer
        testCustomer = Customer.builder()
                .customerId(888000001L)
                .firstName("Repo")
                .lastName("Test")
                .build();
        customerRepository.save(testCustomer);
    }

    @Test
    void userRepository_findByUserIdStartingWith_shouldReturnMatchingUsers() {
        userRepository.save(User.builder().userId("ADMIN001").firstName("Admin").lastName("One").userType("A").build());
        userRepository.save(User.builder().userId("ADMIN002").firstName("Admin").lastName("Two").userType("A").build());
        userRepository.save(User.builder().userId("USER0001").firstName("User").lastName("One").userType("U").build());

        Page<User> admins = userRepository.findByUserIdStartingWith("ADMIN", PageRequest.of(0, 10));
        assertThat(admins.getContent()).hasSize(2);
        assertThat(admins.getContent()).allMatch(u -> u.getUserId().startsWith("ADMIN"));

        Page<User> users = userRepository.findByUserIdStartingWith("USER", PageRequest.of(0, 10));
        assertThat(users.getContent()).hasSize(1);
    }

    @Test
    void cardDataRepository_findByAccountId_shouldReturnCardsForAccount() {
        cardDataRepository.save(CardData.builder()
                .cardNumber("1111000011110001")
                .account(testAccount1)
                .cvvCode(111)
                .embossedName("CARD ONE")
                .cardStatus("Y")
                .build());

        cardDataRepository.save(CardData.builder()
                .cardNumber("1111000011110002")
                .account(testAccount1)
                .cvvCode(222)
                .embossedName("CARD TWO")
                .cardStatus("Y")
                .build());

        cardDataRepository.save(CardData.builder()
                .cardNumber("2222000022220001")
                .account(testAccount2)
                .cvvCode(333)
                .embossedName("OTHER CARD")
                .cardStatus("Y")
                .build());

        List<CardData> account1Cards = cardDataRepository.findByAccountId(11111111111L);
        assertThat(account1Cards).hasSize(2);

        List<CardData> account2Cards = cardDataRepository.findByAccountId(22222222222L);
        assertThat(account2Cards).hasSize(1);
    }

    @Test
    void cardCrossReferenceRepository_findByAccountId_shouldReturnXrefsForAccount() {
        cardCrossReferenceRepository.save(CardCrossReference.builder()
                .cardNumber("3333000033330001")
                .account(testAccount1)
                .customer(testCustomer)
                .build());

        cardCrossReferenceRepository.save(CardCrossReference.builder()
                .cardNumber("3333000033330002")
                .account(testAccount1)
                .customer(testCustomer)
                .build());

        List<CardCrossReference> xrefs = cardCrossReferenceRepository.findByAccountId(11111111111L);
        assertThat(xrefs).hasSize(2);

        List<CardCrossReference> noXrefs = cardCrossReferenceRepository.findByAccountId(22222222222L);
        assertThat(noXrefs).isEmpty();
    }

    @Test
    void cardCrossReferenceRepository_findByCustomerId_shouldReturnXrefsForCustomer() {
        cardCrossReferenceRepository.save(CardCrossReference.builder()
                .cardNumber("4444000044440001")
                .account(testAccount1)
                .customer(testCustomer)
                .build());

        List<CardCrossReference> xrefs = cardCrossReferenceRepository.findByCustomerId(888000001L);
        assertThat(xrefs).hasSize(1);

        List<CardCrossReference> noXrefs = cardCrossReferenceRepository.findByCustomerId(999999999L);
        assertThat(noXrefs).isEmpty();
    }

    @Test
    void transactionRepository_findByCardNumber_shouldReturnPagedTransactions() {
        String cardNum = "5555000055550001";
        for (int i = 1; i <= 5; i++) {
            transactionRepository.save(Transaction.builder()
                    .transactionId(String.format("TXN%013d", i))
                    .transactionType("PU")
                    .transactionCategory(5411)
                    .transactionAmount(new BigDecimal("10.00"))
                    .cardNumber(cardNum)
                    .build());
        }

        // Add a transaction for a different card
        transactionRepository.save(Transaction.builder()
                .transactionId("TXN0000000000099")
                .transactionType("PU")
                .transactionAmount(new BigDecimal("50.00"))
                .cardNumber("9999000099990001")
                .build());

        Page<Transaction> page1 = transactionRepository.findByCardNumber(cardNum, PageRequest.of(0, 3));
        assertThat(page1.getContent()).hasSize(3);
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        Page<Transaction> page2 = transactionRepository.findByCardNumber(cardNum, PageRequest.of(1, 3));
        assertThat(page2.getContent()).hasSize(2);
    }
}
