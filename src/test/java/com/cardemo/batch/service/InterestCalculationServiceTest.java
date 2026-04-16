package com.cardemo.batch.service;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterestCalculationService.
 * Tests individual business rules ported from CBACT04C.cbl paragraphs.
 */
@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceTest {

    @Mock
    private DisclosureGroupRepository disclosureGroupRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    private InterestCalculationService service;

    @BeforeEach
    void setUp() {
        service = new InterestCalculationService(
                disclosureGroupRepository,
                accountRepository,
                cardXrefRepository,
                transactionRecordRepository,
                new SimpleMeterRegistry());
        service.resetCounters();
    }

    // --- Test 1: Interest formula with BigDecimal ---
    @Test
    @DisplayName("1300-COMPUTE-INTEREST: (balance * rate) / 1200 with BigDecimal")
    void computeMonthlyInterest_basicCalculation() {
        BigDecimal balance = new BigDecimal("12000.00");
        BigDecimal rate = new BigDecimal("18.00");

        BigDecimal result = service.computeMonthlyInterest(balance, rate);

        // (12000 * 18) / 1200 = 180.00
        assertEquals(new BigDecimal("180.00"), result);
    }

    // --- Test 2: Interest formula with fractional rate ---
    @Test
    @DisplayName("1300-COMPUTE-INTEREST: handles fractional rates correctly")
    void computeMonthlyInterest_fractionalRate() {
        BigDecimal balance = new BigDecimal("5000.00");
        BigDecimal rate = new BigDecimal("15.50");

        BigDecimal result = service.computeMonthlyInterest(balance, rate);

        // (5000 * 15.50) / 1200 = 64.58 (rounded HALF_UP)
        assertEquals(new BigDecimal("64.58"), result);
    }

    // --- Test 3: Interest formula with zero balance ---
    @Test
    @DisplayName("1300-COMPUTE-INTEREST: zero balance yields zero interest")
    void computeMonthlyInterest_zeroBalance() {
        BigDecimal result = service.computeMonthlyInterest(BigDecimal.ZERO, new BigDecimal("18.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    // --- Test 4: Interest formula with null inputs ---
    @Test
    @DisplayName("1300-COMPUTE-INTEREST: null inputs return zero")
    void computeMonthlyInterest_nullInputs() {
        assertEquals(BigDecimal.ZERO, service.computeMonthlyInterest(null, new BigDecimal("18.00")));
        assertEquals(BigDecimal.ZERO, service.computeMonthlyInterest(new BigDecimal("1000"), null));
    }

    // --- Test 5: Disclosure group lookup - found directly ---
    @Test
    @DisplayName("1200-GET-INTEREST-RATE: finds rate by group ID directly")
    void getInterestRate_directLookup() {
        DisclosureGroup dg = new DisclosureGroup("GRP001", "01", "0001", new BigDecimal("18.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GRP001", "01", "0001"))
                .thenReturn(Optional.of(dg));

        BigDecimal rate = service.getInterestRate("GRP001", "01", "0001");
        assertEquals(new BigDecimal("18.00"), rate);

        verify(disclosureGroupRepository, times(1))
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd("GRP001", "01", "0001");
        verify(disclosureGroupRepository, never())
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(eq("DEFAULT"), any(), any());
    }

    // --- Test 6: Disclosure group fallback to DEFAULT ---
    @Test
    @DisplayName("1200-A-GET-DEFAULT-INT-RATE: falls back to DEFAULT group when not found")
    void getInterestRate_fallbackToDefault() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("GRP999", "01", "0001"))
                .thenReturn(Optional.empty());
        DisclosureGroup defaultDg = new DisclosureGroup("DEFAULT", "01", "0001", new BigDecimal("12.00"));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "0001"))
                .thenReturn(Optional.of(defaultDg));

        BigDecimal rate = service.getInterestRate("GRP999", "01", "0001");
        assertEquals(new BigDecimal("12.00"), rate);

        verify(disclosureGroupRepository).findByAcctGroupIdAndTranTypeCdAndTranCatCd("GRP999", "01", "0001");
        verify(disclosureGroupRepository).findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", "0001");
    }

    // --- Test 7: Disclosure group - neither found returns zero ---
    @Test
    @DisplayName("1200-GET-INTEREST-RATE: returns zero when both lookups fail")
    void getInterestRate_neitherFound() {
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd(any(), any(), any()))
                .thenReturn(Optional.empty());

        BigDecimal rate = service.getInterestRate("NONEXIST", "01", "0001");
        assertEquals(BigDecimal.ZERO, rate);
    }

    // --- Test 8: Transaction generation - correct fields ---
    @Test
    @DisplayName("1300-B-WRITE-TX: generated transaction has correct type, category, source")
    void generateInterestTransaction_correctFields() {
        TransactionRecord txn = service.generateInterestTransaction(
                "2024-01-15", "00000012345", new BigDecimal("180.00"), "4111111111111111");

        assertEquals("01", txn.getTranTypeCd());
        assertEquals("05", txn.getTranCatCd());
        assertEquals("System", txn.getTranSource());
        assertEquals("Int. for a/c 00000012345", txn.getTranDesc());
        assertEquals(new BigDecimal("180.00"), txn.getTranAmt());
        assertEquals("4111111111111111", txn.getCardNum());
    }

    // --- Test 9: Transaction ID format ---
    @Test
    @DisplayName("1300-B-WRITE-TX: transaction ID = PARM-DATE + 6-digit auto-incrementing suffix")
    void generateInterestTransaction_transactionIdFormat() {
        service.resetCounters();

        TransactionRecord txn1 = service.generateInterestTransaction(
                "2024-01-15", "00000012345", new BigDecimal("100.00"), "4111111111111111");
        TransactionRecord txn2 = service.generateInterestTransaction(
                "2024-01-15", "00000012345", new BigDecimal("200.00"), "4111111111111111");

        assertEquals("2024-01-15000001", txn1.getTranId());
        assertEquals("2024-01-15000002", txn2.getTranId());
    }

    // --- Test 10: Transaction has DB2-format timestamp ---
    @Test
    @DisplayName("1300-B-WRITE-TX: transaction has DB2-format timestamps")
    void generateInterestTransaction_hasTimestamps() {
        TransactionRecord txn = service.generateInterestTransaction(
                "2024-01-15", "00000012345", new BigDecimal("100.00"), "4111111111111111");

        assertNotNull(txn.getOrigTs());
        assertNotNull(txn.getProcTs());
        // DB2 format: YYYY-MM-DD-HH.MM.SS.NNNNNN
        assertTrue(txn.getOrigTs().matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{6}"),
                "Timestamp should match DB2 format: " + txn.getOrigTs());
    }

    // --- Test 11: Account update - balance and cycle reset ---
    @Test
    @DisplayName("1050-UPDATE-ACCOUNT: adds interest to balance and resets cycle counters")
    void updateAccount_balanceAndCycleReset() {
        Account account = new Account();
        account.setAcctId("00000012345");
        account.setCurrBal(new BigDecimal("5000.00"));
        account.setCurrCycCredit(new BigDecimal("1000.00"));
        account.setCurrCycDebit(new BigDecimal("500.00"));

        when(accountRepository.findById("00000012345")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        service.updateAccount("00000012345", new BigDecimal("180.00"));

        assertEquals(new BigDecimal("5180.00"), account.getCurrBal());
        assertEquals(BigDecimal.ZERO, account.getCurrCycCredit());
        assertEquals(BigDecimal.ZERO, account.getCurrCycDebit());
        verify(accountRepository).save(account);
    }

    // --- Test 12: Account update - account not found throws ---
    @Test
    @DisplayName("1050-UPDATE-ACCOUNT: throws when account not found")
    void updateAccount_notFound() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.updateAccount("99999999999", BigDecimal.ZERO));
    }

    // --- Test 13: Fee computation is a no-op stub ---
    @Test
    @DisplayName("1400-COMPUTE-FEES: stub returns zero")
    void computeFees_returnsZero() {
        TransactionCategoryBalance catBal = new TransactionCategoryBalance(
                "00000012345", "01", "0001", new BigDecimal("5000.00"));

        BigDecimal fee = service.computeFees(catBal);
        assertEquals(BigDecimal.ZERO, fee);
    }

    // --- Test 14: Card number lookup ---
    @Test
    @DisplayName("1110-GET-XREF-DATA: looks up card number by account ID")
    void lookupCardNumber_found() {
        CardXref xref = new CardXref("4111111111111111", "000000001", "00000012345");
        when(cardXrefRepository.findFirstByAcctId("00000012345")).thenReturn(Optional.of(xref));

        String cardNum = service.lookupCardNumber("00000012345");
        assertEquals("4111111111111111", cardNum);
    }

    // --- Test 15: Card number lookup - not found returns empty ---
    @Test
    @DisplayName("1110-GET-XREF-DATA: returns empty string when xref not found")
    void lookupCardNumber_notFound() {
        when(cardXrefRepository.findFirstByAcctId("99999999999")).thenReturn(Optional.empty());

        String cardNum = service.lookupCardNumber("99999999999");
        assertEquals("", cardNum);
    }
}
