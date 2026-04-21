package com.cardemo.equivalence.integration;

import com.cardemo.batch.BatchTransactionPostingApplication;
import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.service.TransactionValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for account expiration check with real database.
 * Business rule 9: Compare ACCT-EXPIRAION-DATE with transaction timestamp.
 */
@SpringBootTest(classes = BatchTransactionPostingApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Account Expiration Integration Tests")
class AccountExpirationIntegrationTest {

    @Autowired
    private TransactionValidationService validationService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private void setupAccountAndCard(String cardNum, long acctId, String expDate) {
        Account account = new Account();
        account.setAcctId(acctId);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCreditLimit(new BigDecimal("99999.99"));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate(expDate);
        account.setGroupId("STANDARD");
        accountRepository.save(account);

        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);
        cardXrefRepository.save(xref);
    }

    private DailyTransaction createTxn(String cardNum, String timestamp) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("IT_EXP");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("10.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp(timestamp);
        return dt;
    }

    @Test
    @DisplayName("IT: Non-expired account passes validation")
    void nonExpiredAccount_passes() {
        setupAccountAndCard("CARD_VALID_EXP", 70001L, "2025-12-31");
        ValidationResult result = validationService.validate(
                createTxn("CARD_VALID_EXP", "2024-06-15-10.30.00.000000"));
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("IT: Expired account fails with code 103")
    void expiredAccount_failsWith103() {
        setupAccountAndCard("CARD_EXPIRED", 70002L, "2023-01-01");
        ValidationResult result = validationService.validate(
                createTxn("CARD_EXPIRED", "2024-06-15-10.30.00.000000"));
        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }

    @Test
    @DisplayName("IT: Same date as expiration passes (COBOL uses >=)")
    void sameDate_passes() {
        setupAccountAndCard("CARD_SAME_DATE", 70003L, "2024-06-15");
        ValidationResult result = validationService.validate(
                createTxn("CARD_SAME_DATE", "2024-06-15-10.30.00.000000"));
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("IT: One day expired fails")
    void oneDayExpired_fails() {
        setupAccountAndCard("CARD_1DAY_EXP", 70004L, "2024-06-14");
        ValidationResult result = validationService.validate(
                createTxn("CARD_1DAY_EXP", "2024-06-15-10.30.00.000000"));
        assertFalse(result.isValid());
        assertEquals(103, result.getFailReasonCode());
    }

    @Test
    @DisplayName("IT: Far future expiration passes")
    void farFutureExpiration_passes() {
        setupAccountAndCard("CARD_FAR_FUTURE", 70005L, "2099-12-31");
        ValidationResult result = validationService.validate(
                createTxn("CARD_FAR_FUTURE", "2024-06-15-10.30.00.000000"));
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("IT: Error message matches COBOL character-for-character")
    void errorMessage_matchesCOBOL() {
        setupAccountAndCard("CARD_MSG_CHECK", 70006L, "2020-01-01");
        ValidationResult result = validationService.validate(
                createTxn("CARD_MSG_CHECK", "2024-06-15-10.30.00.000000"));
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                result.getFailReasonDescription());
    }
}
