package com.carddemo.entity;

import com.carddemo.enums.UserType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JPA entity field types and constructors.
 * Verifies all monetary fields use BigDecimal and date fields use java.time.
 */
class EntityMappingTest {

    @Test
    void account_monetaryFieldsAreBigDecimal() {
        Account account = new Account();
        account.setCurrentBalance(new BigDecimal("1234.56"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCashCreditLimit(new BigDecimal("1000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("1234.56"), account.getCurrentBalance());
        assertEquals(new BigDecimal("5000.00"), account.getCreditLimit());
        assertEquals(new BigDecimal("1000.00"), account.getCashCreditLimit());
    }

    @Test
    void account_dateFieldsAreLocalDate() {
        Account account = new Account();
        LocalDate openDate = LocalDate.of(2020, 1, 15);
        LocalDate expDate = LocalDate.of(2025, 12, 31);
        account.setOpenDate(openDate);
        account.setExpirationDate(expDate);

        assertEquals(openDate, account.getOpenDate());
        assertEquals(expDate, account.getExpirationDate());
    }

    @Test
    void userSecurity_userTypeEnum() {
        UserSecurity user = new UserSecurity();
        user.setUserId("test001");
        user.setPassword("password");
        user.setUserType(UserType.ADMIN);

        assertEquals(UserType.ADMIN, user.getUserType());
        assertEquals('A', user.getUserType().getCode());
    }

    @Test
    void userType_fromCode() {
        assertEquals(UserType.ADMIN, UserType.fromCode('A'));
        assertEquals(UserType.USER, UserType.fromCode('U'));
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode('X'));
    }

    @Test
    void card_primaryKeyIsString() {
        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setAcctId(1L);
        card.setActiveStatus("Y");

        assertEquals("4111111111111111", card.getCardNum());
        assertEquals(1L, card.getAcctId());
    }

    @Test
    void transaction_amountIsBigDecimal() {
        Transaction txn = new Transaction();
        txn.setAmount(new BigDecimal("99.99"));
        txn.setOrigTimestamp(LocalDateTime.of(2024, 6, 15, 10, 30, 0));

        assertEquals(new BigDecimal("99.99"), txn.getAmount());
        assertEquals(2024, txn.getOrigTimestamp().getYear());
    }

    @Test
    void customer_fieldsMap() {
        Customer customer = new Customer();
        customer.setCustId(1001L);
        customer.setFirstName("John");
        customer.setMiddleName("M");
        customer.setLastName("Doe");
        customer.setSsn("123456789");

        assertEquals(1001L, customer.getCustId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
    }

    @Test
    void disclosureGroup_interestRateIsBigDecimal() {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setInterestRate(new BigDecimal("18.99"));

        assertEquals(new BigDecimal("18.99"), dg.getInterestRate());
    }

    @Test
    void transactionCategoryBalance_balanceIsBigDecimal() {
        TransactionCategoryBalance tcb = new TransactionCategoryBalance();
        tcb.setBalance(new BigDecimal("5432.10"));

        assertEquals(new BigDecimal("5432.10"), tcb.getBalance());
    }

    @Test
    void cardXref_mapping() {
        CardXref xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setAcctId(100L);
        xref.setCustId(200L);

        assertEquals("4111111111111111", xref.getCardNum());
        assertEquals(100L, xref.getAcctId());
        assertEquals(200L, xref.getCustId());
    }
}
