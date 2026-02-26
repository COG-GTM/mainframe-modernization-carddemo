package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.AddTransactionResponse;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.CardCrossReference;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.exception.AccountNotFoundException;
import com.carddemo.transaction.exception.CardNotFoundException;
import com.carddemo.transaction.exception.TransactionNotFoundException;
import com.carddemo.transaction.repository.CardCrossReferenceRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionServiceImpl.
 *
 * Tests the migrated business logic from COTRN02C PROCEDURE DIVISION:
 *   - Cross-reference lookups (VALIDATE-INPUT-KEY-FIELDS)
 *   - Transaction creation (ADD-TRANSACTION)
 *   - Latest transaction retrieval (COPY-LAST-TRAN-DATA)
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AddTransactionRequest validRequest;
    private CardCrossReference sampleXref;

    @BeforeEach
    void setUp() {
        validRequest = new AddTransactionRequest();
        validRequest.setCardNumber("4000123456789010");
        validRequest.setTypeCode("01");
        validRequest.setCategoryCode(5411);
        validRequest.setSource("ONLINE");
        validRequest.setDescription("Test purchase");
        validRequest.setAmount(new BigDecimal("-125.50"));
        validRequest.setOriginatedDate(LocalDate.of(2026, 2, 24));
        validRequest.setProcessedDate(LocalDate.of(2026, 2, 24));
        validRequest.setMerchantId(123456789L);
        validRequest.setMerchantName("Test Store");
        validRequest.setMerchantCity("Seattle");
        validRequest.setMerchantZip("98101");

        sampleXref = new CardCrossReference();
        sampleXref.setCardNumber("4000123456789010");
        sampleXref.setAccountId(12345678901L);
        sampleXref.setCustomerId(123456789L);
    }

    @Nested
    @DisplayName("addTransaction - replaces ADD-TRANSACTION paragraph")
    class AddTransactionTests {

        @Test
        @DisplayName("Successfully adds transaction with card number lookup")
        void addTransaction_withCardNumber_success() {
            // Replaces: WHEN CARDNINI NOT = SPACES -> READ CCXREF -> WRITE TRANSACT
            when(cardCrossReferenceRepository.findByCardNumber("4000123456789010"))
                    .thenReturn(Optional.of(sampleXref));

            Transaction savedTransaction = new Transaction();
            savedTransaction.setTransactionId(42L);
            savedTransaction.setCardNumber("4000123456789010");
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(savedTransaction);

            AddTransactionResponse response = transactionService.addTransaction(validRequest);

            assertNotNull(response);
            assertEquals(42L, response.getTransactionId());
            assertTrue(response.getMessage().contains("Transaction added successfully"));
            assertTrue(response.getMessage().contains("42"));
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Successfully adds transaction with account ID lookup")
        void addTransaction_withAccountId_success() {
            // Replaces: WHEN ACTIDINI NOT = SPACES -> READ CXACAIX -> WRITE TRANSACT
            validRequest.setCardNumber(null);
            validRequest.setAccountId(12345678901L);

            when(cardCrossReferenceRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(sampleXref));

            Transaction savedTransaction = new Transaction();
            savedTransaction.setTransactionId(43L);
            savedTransaction.setCardNumber("4000123456789010");
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(savedTransaction);

            AddTransactionResponse response = transactionService.addTransaction(validRequest);

            assertNotNull(response);
            assertEquals(43L, response.getTransactionId());
            verify(cardCrossReferenceRepository).findByAccountId(12345678901L);
        }

        @Test
        @DisplayName("Throws CardNotFoundException when card number not in XREF")
        void addTransaction_cardNotFound_throwsException() {
            // Replaces: DFHRESP(NOTFND) in READ-CCXREF-FILE
            //   "Card Number NOT found..."
            when(cardCrossReferenceRepository.findByCardNumber("4000123456789010"))
                    .thenReturn(Optional.empty());

            CardNotFoundException exception = assertThrows(
                    CardNotFoundException.class,
                    () -> transactionService.addTransaction(validRequest)
            );

            assertTrue(exception.getMessage().contains("4000123456789010"));
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws AccountNotFoundException when account ID not in XREF")
        void addTransaction_accountNotFound_throwsException() {
            // Replaces: DFHRESP(NOTFND) in READ-CXACAIX-FILE
            //   "Account ID NOT found..."
            validRequest.setCardNumber(null);
            validRequest.setAccountId(99999999999L);

            when(cardCrossReferenceRepository.findByAccountId(99999999999L))
                    .thenReturn(Optional.empty());

            AccountNotFoundException exception = assertThrows(
                    AccountNotFoundException.class,
                    () -> transactionService.addTransaction(validRequest)
            );

            assertTrue(exception.getMessage().contains("99999999999"));
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Maps all request fields correctly to entity")
        void addTransaction_mapsAllFields() {
            // Verifies all MOVE statements in ADD-TRANSACTION paragraph
            when(cardCrossReferenceRepository.findByCardNumber("4000123456789010"))
                    .thenReturn(Optional.of(sampleXref));

            Transaction savedTransaction = new Transaction();
            savedTransaction.setTransactionId(1L);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> {
                        Transaction t = invocation.getArgument(0);
                        assertEquals("01", t.getTypeCode());
                        assertEquals(5411, t.getCategoryCode());
                        assertEquals("ONLINE", t.getSource());
                        assertEquals("Test purchase", t.getDescription());
                        assertEquals(new BigDecimal("-125.50"), t.getAmount());
                        assertEquals("4000123456789010", t.getCardNumber());
                        assertEquals(123456789L, t.getMerchantId());
                        assertEquals("Test Store", t.getMerchantName());
                        assertEquals("Seattle", t.getMerchantCity());
                        assertEquals("98101", t.getMerchantZip());
                        assertNotNull(t.getOriginatedTs());
                        assertNotNull(t.getProcessedTs());
                        t.setTransactionId(1L);
                        return t;
                    });

            transactionService.addTransaction(validRequest);
            verify(transactionRepository).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("getTransaction - replaces direct VSAM READ")
    class GetTransactionTests {

        @Test
        @DisplayName("Returns transaction when found")
        void getTransaction_found() {
            Transaction transaction = createSampleTransaction(1L);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

            TransactionResponse response = transactionService.getTransaction(1L);

            assertNotNull(response);
            assertEquals(1L, response.getTransactionId());
            assertEquals("01", response.getTypeCode());
            assertEquals(5411, response.getCategoryCode());
        }

        @Test
        @DisplayName("Throws TransactionNotFoundException when not found")
        void getTransaction_notFound() {
            // Replaces: DFHRESP(NOTFND) in STARTBR-TRANSACT-FILE
            when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    TransactionNotFoundException.class,
                    () -> transactionService.getTransaction(999L)
            );
        }
    }

    @Nested
    @DisplayName("getLatestTransaction - replaces COPY-LAST-TRAN-DATA (PF5)")
    class GetLatestTransactionTests {

        @Test
        @DisplayName("Returns the latest transaction")
        void getLatestTransaction_found() {
            // Replaces: STARTBR HIGH-VALUES, READPREV, ENDBR
            Transaction transaction = createSampleTransaction(100L);
            when(transactionRepository.findFirstByOrderByTransactionIdDesc())
                    .thenReturn(Optional.of(transaction));

            TransactionResponse response = transactionService.getLatestTransaction();

            assertNotNull(response);
            assertEquals(100L, response.getTransactionId());
        }

        @Test
        @DisplayName("Throws when no transactions exist")
        void getLatestTransaction_empty() {
            // Replaces: DFHRESP(ENDFILE) in READPREV-TRANSACT-FILE
            when(transactionRepository.findFirstByOrderByTransactionIdDesc())
                    .thenReturn(Optional.empty());

            assertThrows(
                    TransactionNotFoundException.class,
                    () -> transactionService.getLatestTransaction()
            );
        }
    }

    private Transaction createSampleTransaction(Long id) {
        Transaction t = new Transaction();
        t.setTransactionId(id);
        t.setTypeCode("01");
        t.setCategoryCode(5411);
        t.setSource("ONLINE");
        t.setDescription("Sample transaction");
        t.setAmount(new BigDecimal("50.00"));
        t.setMerchantId(123456789L);
        t.setMerchantName("Test Store");
        t.setMerchantCity("Seattle");
        t.setMerchantZip("98101");
        t.setCardNumber("4000123456789010");
        t.setOriginatedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        t.setProcessedTs(LocalDateTime.of(2026, 2, 24, 0, 0));
        return t;
    }
}
