package com.carddemo.batch.processor;

import com.carddemo.batch.model.Account;
import com.carddemo.batch.model.Card;
import com.carddemo.batch.model.CardCrossReference;
import com.carddemo.batch.model.DailyTransaction;
import com.carddemo.batch.model.Transaction;
import com.carddemo.batch.model.TransactionCategoryBalance;
import com.carddemo.batch.model.TransactionCategoryBalanceId;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.CardRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionPostingProcessor.
 * Validates the business logic migrated from COBOL programs CBTRN01C + CBTRN02C.
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingProcessorTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardCrossReferenceRepository xrefRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionCategoryBalanceRepository tcatBalRepository;

    @InjectMocks
    private TransactionPostingProcessor processor;

    private DailyTransaction dailyTransaction;
    private Card card;
    private CardCrossReference xref;
    private Account account;

    @BeforeEach
    void setUp() {
        dailyTransaction = DailyTransaction.builder()
                .tranId("DT20260415000001")
                .typeCd("02")
                .catCd(5001)
                .source("POS")
                .description("GROCERY STORE PURCHASE")
                .amount(new BigDecimal("-125.50"))
                .merchantId(900000001L)
                .merchantName("WHOLE FOODS MARKET")
                .merchantCity("CHICAGO")
                .merchantZip("60601")
                .cardNum("4111111111111111")
                .origTimestamp("2026-04-15-10.30.00.000000")
                .procTimestamp("")
                .processed(false)
                .build();

        card = Card.builder()
                .cardNum("4111111111111111")
                .acctId(10000000001L)
                .cvvCode(123)
                .embossedName("JOHN A SMITH")
                .expirationDate("2027-01-15")
                .activeStatus("Y")
                .build();

        xref = CardCrossReference.builder()
                .cardNum("4111111111111111")
                .custId(100000001L)
                .acctId(10000000001L)
                .build();

        account = Account.builder()
                .acctId(10000000001L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("5000.00"))
                .creditLimit(new BigDecimal("15000.00"))
                .cashCreditLimit(new BigDecimal("5000.00"))
                .openDate("2020-01-15")
                .expirationDate("2027-01-15")
                .reissueDate("2025-01-15")
                .currentCycleCredit(new BigDecimal("500.00"))
                .currentCycleDebit(new BigDecimal("-200.00"))
                .addrZip("60601")
                .groupId("GROUP01")
                .build();
    }

    @Test
    @DisplayName("Should successfully post a valid transaction")
    void shouldPostValidTransaction() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class)))
                .thenReturn(Optional.empty());

        Transaction result = processor.process(dailyTransaction);

        assertThat(result).isNotNull();
        assertThat(result.getTranId()).isEqualTo("DT20260415000001");
        assertThat(result.getTypeCd()).isEqualTo("02");
        assertThat(result.getCatCd()).isEqualTo(5001);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("-125.50"));
        assertThat(result.getCardNum()).isEqualTo("4111111111111111");
        assertThat(result.getProcTimestamp()).isNotEmpty();
    }

    @Test
    @DisplayName("Should reject transaction with invalid card number - mirrors CBTRN02C 1500-A-LOOKUP-XREF")
    void shouldRejectInvalidCardNumber() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.empty());

        Transaction result = processor.process(dailyTransaction);

        assertThat(result).isNull();
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject transaction when xref not found")
    void shouldRejectWhenXrefNotFound() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.empty());

        Transaction result = processor.process(dailyTransaction);

        assertThat(result).isNull();
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject transaction when account not found - mirrors CBTRN02C 1500-B-LOOKUP-ACCT")
    void shouldRejectWhenAccountNotFound() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.empty());

        Transaction result = processor.process(dailyTransaction);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should reject overlimit transaction - mirrors CBTRN02C credit limit validation")
    void shouldRejectOverlimitTransaction() throws Exception {
        // Set up a transaction that exceeds credit limit
        dailyTransaction.setAmount(new BigDecimal("20000.00"));

        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));

        Transaction result = processor.process(dailyTransaction);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should update account balance correctly for debit - mirrors CBTRN02C 2800-UPDATE-ACCOUNT-REC")
    void shouldUpdateAccountBalanceForDebit() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class)))
                .thenReturn(Optional.empty());

        processor.process(dailyTransaction);

        // Verify account balance updated: 5000.00 + (-125.50) = 4874.50
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getCurrentBalance())
                .isEqualByComparingTo(new BigDecimal("4874.50"));
        // Debit should be added to cycle debit: -200.00 + (-125.50) = -325.50
        assertThat(savedAccount.getCurrentCycleDebit())
                .isEqualByComparingTo(new BigDecimal("-325.50"));
    }

    @Test
    @DisplayName("Should update account balance correctly for credit - mirrors CBTRN02C 2800-UPDATE-ACCOUNT-REC")
    void shouldUpdateAccountBalanceForCredit() throws Exception {
        dailyTransaction.setAmount(new BigDecimal("500.00"));

        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class)))
                .thenReturn(Optional.empty());

        processor.process(dailyTransaction);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        // Balance: 5000.00 + 500.00 = 5500.00
        assertThat(savedAccount.getCurrentBalance())
                .isEqualByComparingTo(new BigDecimal("5500.00"));
        // Credit should be added to cycle credit: 500.00 + 500.00 = 1000.00
        assertThat(savedAccount.getCurrentCycleCredit())
                .isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Should create new transaction category balance when not found - mirrors CBTRN02C 2700-A-CREATE")
    void shouldCreateNewTransactionCategoryBalance() throws Exception {
        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class)))
                .thenReturn(Optional.empty());

        processor.process(dailyTransaction);

        ArgumentCaptor<TransactionCategoryBalance> tcatCaptor =
                ArgumentCaptor.forClass(TransactionCategoryBalance.class);
        verify(tcatBalRepository).save(tcatCaptor.capture());
        TransactionCategoryBalance savedTcat = tcatCaptor.getValue();
        assertThat(savedTcat.getAcctId()).isEqualTo(10000000001L);
        assertThat(savedTcat.getTypeCd()).isEqualTo("02");
        assertThat(savedTcat.getCatCd()).isEqualTo(5001);
        assertThat(savedTcat.getBalance()).isEqualByComparingTo(new BigDecimal("-125.50"));
    }

    @Test
    @DisplayName("Should update existing transaction category balance - mirrors CBTRN02C 2700-B-UPDATE")
    void shouldUpdateExistingTransactionCategoryBalance() throws Exception {
        TransactionCategoryBalance existingTcat = TransactionCategoryBalance.builder()
                .acctId(10000000001L)
                .typeCd("02")
                .catCd(5001)
                .balance(new BigDecimal("-1250.00"))
                .build();

        when(cardRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(card));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class)))
                .thenReturn(Optional.of(existingTcat));

        processor.process(dailyTransaction);

        ArgumentCaptor<TransactionCategoryBalance> tcatCaptor =
                ArgumentCaptor.forClass(TransactionCategoryBalance.class);
        verify(tcatBalRepository).save(tcatCaptor.capture());
        TransactionCategoryBalance savedTcat = tcatCaptor.getValue();
        // -1250.00 + (-125.50) = -1375.50
        assertThat(savedTcat.getBalance()).isEqualByComparingTo(new BigDecimal("-1375.50"));
    }
}
