package com.carddemo.common.model;

import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.dto.LoginRequest;
import com.carddemo.common.dto.TransactionDto;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import com.carddemo.common.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityTest {

    @Test
    void testAccountCreationAndEquality() {
        Account account = Account.builder()
                .accountId(12345678901L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("1000.50"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("2000.00"))
                .openDate("2023-01-15")
                .expirationDate("2028-01-15")
                .reissueDate("2025-01-15")
                .currentCycleCredit(new BigDecimal("500.00"))
                .currentCycleDebit(new BigDecimal("200.00"))
                .addressZip("10001")
                .groupId("GRP001")
                .build();

        assertNotNull(account);
        assertEquals(12345678901L, account.getAccountId());
        assertEquals("Y", account.getActiveStatus());
        assertEquals(new BigDecimal("1000.50"), account.getCurrentBalance());
        assertEquals("2023-01-15", account.getOpenDate());

        Account sameAccount = Account.builder().accountId(12345678901L).build();
        Account differentAccount = Account.builder().accountId(99999999999L).build();

        assertEquals(account, account);
        assertNotEquals(account, differentAccount);
    }

    @Test
    void testCardCreation() {
        Card card = Card.builder()
                .cardNumber("4111111111111111")
                .accountId(12345678901L)
                .cvvCode(123)
                .embossedName("JOHN DOE")
                .expirationDate("2028-12-31")
                .activeStatus("Y")
                .build();

        assertNotNull(card);
        assertEquals("4111111111111111", card.getCardNumber());
        assertEquals(12345678901L, card.getAccountId());
        assertEquals(123, card.getCvvCode());
        assertEquals("JOHN DOE", card.getEmbossedName());
    }

    @Test
    void testCardCrossReferenceCreation() {
        CardCrossReference xref = CardCrossReference.builder()
                .cardNumber("4111111111111111")
                .customerId(123456789L)
                .accountId(12345678901L)
                .build();

        assertNotNull(xref);
        assertEquals("4111111111111111", xref.getCardNumber());
        assertEquals(123456789L, xref.getCustomerId());
        assertEquals(12345678901L, xref.getAccountId());
    }

    @Test
    void testCustomerCreation() {
        Customer customer = Customer.builder()
                .customerId(123456789L)
                .firstName("John")
                .middleName("M")
                .lastName("Doe")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .addressLine3("")
                .stateCode("NY")
                .countryCode("USA")
                .zipCode("10001")
                .phoneNumber1("212-555-0100")
                .phoneNumber2("212-555-0101")
                .ssn(123456789L)
                .governmentIssuedId("DL12345")
                .dateOfBirth("1990-05-15")
                .eftAccountId("EFT001")
                .primaryCardHolderIndicator("Y")
                .ficoCreditScore(750)
                .build();

        assertNotNull(customer);
        assertEquals(123456789L, customer.getCustomerId());
        assertEquals("John", customer.getFirstName());
        assertEquals("NY", customer.getStateCode());
        assertEquals(750, customer.getFicoCreditScore());
    }

    @Test
    void testTransactionCreation() {
        Transaction txn = Transaction.builder()
                .transactionId("TXN0000000000001")
                .typeCode("SA")
                .categoryCode(5001)
                .source("ONLINE")
                .description("Purchase at store")
                .amount(new BigDecimal("99.99"))
                .merchantId(123456789L)
                .merchantName("Test Store")
                .merchantCity("New York")
                .merchantZip("10001")
                .cardNumber("4111111111111111")
                .originTimestamp("2024-01-15-10.30.00.000000")
                .processedTimestamp("2024-01-15-10.30.05.000000")
                .build();

        assertNotNull(txn);
        assertEquals("TXN0000000000001", txn.getTransactionId());
        assertEquals("SA", txn.getTypeCode());
        assertEquals(new BigDecimal("99.99"), txn.getAmount());
    }

    @Test
    void testDailyTransactionCreation() {
        DailyTransaction dtxn = DailyTransaction.builder()
                .transactionId("DTX0000000000001")
                .typeCode("SA")
                .categoryCode(5001)
                .source("ONLINE")
                .description("Daily purchase")
                .amount(new BigDecimal("50.00"))
                .merchantId(123456789L)
                .merchantName("Daily Store")
                .merchantCity("Boston")
                .merchantZip("02101")
                .cardNumber("4111111111111111")
                .originTimestamp("2024-01-15-08.00.00.000000")
                .processedTimestamp("2024-01-15-08.00.02.000000")
                .build();

        assertNotNull(dtxn);
        assertEquals("DTX0000000000001", dtxn.getTransactionId());
        assertEquals(new BigDecimal("50.00"), dtxn.getAmount());
    }

    @Test
    void testTransactionCategoryBalanceCreation() {
        TransactionCategoryBalance.TransactionCategoryBalanceId id =
                TransactionCategoryBalance.TransactionCategoryBalanceId.builder()
                        .accountId(12345678901L)
                        .typeCode("SA")
                        .categoryCode(5001)
                        .build();

        TransactionCategoryBalance balance = TransactionCategoryBalance.builder()
                .id(id)
                .balance(new BigDecimal("1500.75"))
                .build();

        assertNotNull(balance);
        assertNotNull(balance.getId());
        assertEquals(12345678901L, balance.getId().getAccountId());
        assertEquals("SA", balance.getId().getTypeCode());
        assertEquals(5001, balance.getId().getCategoryCode());
        assertEquals(new BigDecimal("1500.75"), balance.getBalance());
    }

    @Test
    void testUserCreation() {
        User user = User.builder()
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .password("PASS1234")
                .userType("A")
                .build();

        assertNotNull(user);
        assertEquals("USER0001", user.getUserId());
        assertEquals("A", user.getUserType());
    }

    @Test
    void testAccountDtoMapping() {
        Account account = Account.builder()
                .accountId(12345678901L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("1000.50"))
                .creditLimit(new BigDecimal("5000.00"))
                .openDate("2023-01-15")
                .groupId("GRP001")
                .build();

        AccountDto dto = AccountDto.builder()
                .accountId(account.getAccountId())
                .activeStatus(account.getActiveStatus())
                .currentBalance(account.getCurrentBalance())
                .creditLimit(account.getCreditLimit())
                .openDate(account.getOpenDate())
                .groupId(account.getGroupId())
                .build();

        assertEquals(account.getAccountId(), dto.getAccountId());
        assertEquals(account.getActiveStatus(), dto.getActiveStatus());
        assertEquals(account.getCurrentBalance(), dto.getCurrentBalance());
    }

    @Test
    void testCardDtoMapping() {
        CardDto dto = CardDto.builder()
                .cardNumber("4111111111111111")
                .accountId(12345678901L)
                .cvvCode(123)
                .embossedName("JOHN DOE")
                .expirationDate("2028-12-31")
                .activeStatus("Y")
                .build();

        assertEquals("4111111111111111", dto.getCardNumber());
        assertEquals("JOHN DOE", dto.getEmbossedName());
    }

    @Test
    void testCustomerDtoMapping() {
        CustomerDto dto = CustomerDto.builder()
                .customerId(123456789L)
                .firstName("Jane")
                .lastName("Smith")
                .stateCode("CA")
                .ficoCreditScore(800)
                .build();

        assertEquals(123456789L, dto.getCustomerId());
        assertEquals("Jane", dto.getFirstName());
        assertEquals(800, dto.getFicoCreditScore());
    }

    @Test
    void testTransactionDtoMapping() {
        TransactionDto dto = TransactionDto.builder()
                .transactionId("TXN0000000000001")
                .typeCode("SA")
                .amount(new BigDecimal("99.99"))
                .cardNumber("4111111111111111")
                .build();

        assertEquals("TXN0000000000001", dto.getTransactionId());
        assertEquals(new BigDecimal("99.99"), dto.getAmount());
    }

    @Test
    void testUserDtoExcludesPassword() {
        UserDto dto = UserDto.builder()
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .userType("A")
                .build();

        assertEquals("USER0001", dto.getUserId());
        assertEquals("A", dto.getUserType());
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = LoginRequest.builder()
                .userId("USER0001")
                .password("PASS1234")
                .build();

        assertEquals("USER0001", request.getUserId());
        assertEquals("PASS1234", request.getPassword());
    }

    @Test
    void testDateUtilParseDate() {
        LocalDate date = DateUtil.parseDate("2024-01-15");
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void testDateUtilParseDateNull() {
        assertNull(DateUtil.parseDate(null));
        assertNull(DateUtil.parseDate(""));
        assertNull(DateUtil.parseDate("   "));
    }

    @Test
    void testDateUtilParseTimestamp() {
        LocalDateTime ts = DateUtil.parseTimestamp("2024-01-15-10.30.00.000000");
        assertNotNull(ts);
        assertEquals(2024, ts.getYear());
        assertEquals(10, ts.getHour());
        assertEquals(30, ts.getMinute());
    }

    @Test
    void testDateUtilParseTimestampNull() {
        assertNull(DateUtil.parseTimestamp(null));
        assertNull(DateUtil.parseTimestamp(""));
    }

    @Test
    void testDateUtilFormatDate() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        assertEquals("2024-01-15", DateUtil.formatDate(date));
        assertNull(DateUtil.formatDate(null));
    }

    @Test
    void testDateUtilFormatTimestamp() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        String formatted = DateUtil.formatTimestamp(dt);
        assertNotNull(formatted);
        assertEquals("2024-01-15-10.30.00.000000", formatted);
        assertNull(DateUtil.formatTimestamp(null));
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Account not found");
        assertEquals("Account not found", ex1.getMessage());

        ResourceNotFoundException ex2 = new ResourceNotFoundException("Account", "id", 123L);
        assertEquals("Account not found with id: '123'", ex2.getMessage());
    }

    @Test
    void testValidationException() {
        ValidationException ex = new ValidationException("Invalid input");
        assertEquals("Invalid input", ex.getMessage());
    }

    @Test
    void testNoArgsConstructors() {
        assertNotNull(new Account());
        assertNotNull(new Card());
        assertNotNull(new CardCrossReference());
        assertNotNull(new Customer());
        assertNotNull(new Transaction());
        assertNotNull(new DailyTransaction());
        assertNotNull(new TransactionCategoryBalance());
        assertNotNull(new User());
    }
}
