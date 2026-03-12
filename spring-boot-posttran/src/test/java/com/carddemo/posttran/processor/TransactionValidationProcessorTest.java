package com.carddemo.posttran.processor;

import com.carddemo.posttran.model.Account;
import com.carddemo.posttran.model.CardXref;
import com.carddemo.posttran.model.DailyTransaction;
import com.carddemo.posttran.model.ProcessedTransaction;
import com.carddemo.posttran.model.ValidationFailReason;
import com.carddemo.posttran.repository.AccountRepository;
import com.carddemo.posttran.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionValidationProcessor.
 * Validates the business logic from COBOL sections 1500-VALIDATE-TRAN,
 * 1500-A-LOOKUP-XREF, and 1500-B-LOOKUP-ACCT.
 */
@ExtendWith(MockitoExtension.class)
class TransactionValidationProcessorTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransactionValidationProcessor(cardXrefRepository, accountRepository);
    }

    private DailyTransaction createDailyTransaction(String cardNum, BigDecimal amount, String origTs) {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("0000000000000001");
        dt.setTranTypeCd("SA");
        dt.setTranCatCd(5001);
        dt.setTranSource("ONLINE");
        dt.setTranDesc("Test transaction");
        dt.setTranAmt(amount);
        dt.setTranMerchantId(123456789L);
        dt.setTranMerchantName("Test Merchant");
        dt.setTranMerchantCity("Test City");
        dt.setTranMerchantZip("12345");
        dt.setTranCardNum(cardNum);
        dt.setTranOrigTs(origTs);
        return dt;
    }

    private CardXref createCardXref(String cardNum, long acctId) {
        CardXref xref = new CardXref();
        xref.setXrefCardNum(cardNum);
        xref.setXrefCustId(100000001L);
        xref.setXrefAcctId(acctId);
        return xref;
    }

    private Account createAccount(long acctId, BigDecimal creditLimit,
                                   BigDecimal cycCredit, BigDecimal cycDebit,
                                   String expirationDate) {
        Account acct = new Account();
        acct.setAcctId(acctId);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(BigDecimal.ZERO);
        acct.setAcctCreditLimit(creditLimit);
        acct.setAcctCashCreditLimit(BigDecimal.ZERO);
        acct.setAcctOpenDate("2020-01-01");
        acct.setAcctExpirationDate(expirationDate);
        acct.setAcctReissueDate("2025-01-01");
        acct.setAcctCurrCycCredit(cycCredit);
        acct.setAcctCurrCycDebit(cycDebit);
        acct.setAcctAddrZip("12345");
        acct.setAcctGroupId("GRP001");
        return acct;
    }

    @Test
    void validTransaction_shouldReturnValid() throws Exception {
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("50.00"),
                        "2027-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(0, result.getValidationFailReason());
        assertEquals(acctId, result.getAccountId());
    }

    @Test
    void invalidCardNumber_shouldRejectWith100() throws Exception {
        String cardNum = "9999999999999999";
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum)).thenReturn(Optional.empty());

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationFailReason.INVALID_CARD_NUMBER.getCode(),
                result.getValidationFailReason());
    }

    @Test
    void accountNotFound_shouldRejectWith101() throws Exception {
        String cardNum = "4111111111111111";
        long acctId = 99999999999L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId)).thenReturn(Optional.empty());

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationFailReason.ACCOUNT_NOT_FOUND.getCode(),
                result.getValidationFailReason());
    }

    @Test
    void overlimitTransaction_shouldRejectWith102() throws Exception {
        // COBOL: WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        // If ACCT-CREDIT-LIMIT < WS-TEMP-BAL -> fail 102
        // cycCredit=4900, cycDebit=0, amount=200 => tempBal=5100 > creditLimit=5000
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("200.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("4900.00"),
                        new BigDecimal("0.00"),
                        "2027-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationFailReason.OVER_LIMIT.getCode(),
                result.getValidationFailReason());
    }

    @Test
    void expiredAccount_shouldRejectWith103() throws Exception {
        // COBOL: IF ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10) -> fail 103
        // expirationDate = "2024-12-31", tranOrigTs starts with "2025-06-15" => expired
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("50.00"),
                        "2024-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationFailReason.ACCOUNT_EXPIRED.getCode(),
                result.getValidationFailReason());
    }

    @Test
    void exactCreditLimit_shouldPassValidation() throws Exception {
        // Edge case: tempBal exactly equals creditLimit should pass (COBOL: >= check)
        // cycCredit=4900, cycDebit=0, amount=100 => tempBal=5000 == creditLimit=5000
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("4900.00"),
                        new BigDecimal("0.00"),
                        "2027-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void overlimitAndExpired_shouldRejectWith103_lastFailureWins() throws Exception {
        // COBOL runs both checks sequentially; last failure overwrites the reason.
        // Both overlimit (102) AND expired (103) → should get 103.
        // cycCredit=4900, cycDebit=0, amount=200 => tempBal=5100 > creditLimit=5000 → overlimit
        // expirationDate="2024-12-31", tranOrigTs="2025-06-15..." → expired
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("200.00"),
                "2025-06-15-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("4900.00"),
                        new BigDecimal("0.00"),
                        "2024-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationFailReason.ACCOUNT_EXPIRED.getCode(),
                result.getValidationFailReason());
    }

    @Test
    void expirationDateEqualsTransactionDate_shouldPass() throws Exception {
        // Edge case: COBOL uses >= so equal dates should pass
        String cardNum = "4111111111111111";
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction(cardNum, new BigDecimal("100.00"),
                "2027-12-31-10.30.00.000000");

        when(cardXrefRepository.findById(cardNum))
                .thenReturn(Optional.of(createCardXref(cardNum, acctId)));
        when(accountRepository.findById(acctId))
                .thenReturn(Optional.of(createAccount(acctId,
                        new BigDecimal("5000.00"),
                        new BigDecimal("200.00"),
                        new BigDecimal("50.00"),
                        "2027-12-31")));

        ProcessedTransaction result = processor.process(dt);

        assertNotNull(result);
        assertTrue(result.isValid());
    }
}
