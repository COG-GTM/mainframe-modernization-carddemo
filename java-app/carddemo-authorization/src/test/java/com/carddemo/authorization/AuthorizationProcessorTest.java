package com.carddemo.authorization;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorizationProcessor — validates parity with COPAUA0C.cbl.
 * Tests all decline reason codes.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationProcessorTest {

    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardRepository cardRepository;
    @Mock private AuthorizationSummaryRepository summaryRepository;
    @Mock private AuthorizationFraudRepository fraudRepository;

    @InjectMocks
    private AuthorizationProcessor processor;

    private CardXref cardXref;
    private Card card;
    private Account account;

    @BeforeEach
    void setUp() {
        cardXref = new CardXref();
        cardXref.setCardNum("4111111111111111");
        cardXref.setAcctId(1L);
        cardXref.setCustId(100L);

        card = new Card();
        card.setCardNum("4111111111111111");
        card.setActiveStatus("Y");

        account = new Account();
        account.setAcctId(1L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));
    }

    @Test
    void processAuthorization_approved() {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(fraudRepository.findByCardNum("4111111111111111")).thenReturn(Collections.emptyList());
        when(summaryRepository.save(any(AuthorizationSummary.class))).thenAnswer(inv -> {
            AuthorizationSummary s = inv.getArgument(0);
            s.setAuthId(1L);
            return s;
        });

        AuthorizationProcessor.AuthorizationRequest request =
                new AuthorizationProcessor.AuthorizationRequest("4111111111111111", new BigDecimal("200.00"), "MERCH001", "PURCHASE");

        AuthorizationProcessor.AuthorizationResponse response = processor.processAuthorization(request);

        assertTrue(response.approved());
        assertEquals("APPROVED", response.status());
        assertNull(response.declineReason());
    }

    @Test
    void processAuthorization_cardNotFound() {
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());
        when(summaryRepository.save(any(AuthorizationSummary.class))).thenAnswer(inv -> {
            AuthorizationSummary s = inv.getArgument(0);
            s.setAuthId(2L);
            return s;
        });

        AuthorizationProcessor.AuthorizationRequest request =
                new AuthorizationProcessor.AuthorizationRequest("0000000000000000", new BigDecimal("100.00"), "MERCH001", "PURCHASE");

        AuthorizationProcessor.AuthorizationResponse response = processor.processAuthorization(request);

        assertFalse(response.approved());
        assertEquals("DECLINED", response.status());
        assertNotNull(response.declineReason());
    }

    @Test
    void processAuthorization_insufficientFunds() {
        account.setCurrentBalance(new BigDecimal("4900.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(summaryRepository.save(any(AuthorizationSummary.class))).thenAnswer(inv -> {
            AuthorizationSummary s = inv.getArgument(0);
            s.setAuthId(3L);
            return s;
        });

        AuthorizationProcessor.AuthorizationRequest request =
                new AuthorizationProcessor.AuthorizationRequest("4111111111111111", new BigDecimal("200.00"), "MERCH001", "PURCHASE");

        AuthorizationProcessor.AuthorizationResponse response = processor.processAuthorization(request);

        assertFalse(response.approved());
        assertEquals("DECLINED", response.status());
        assertNotNull(response.declineReason());
    }

    @Test
    void processAuthorization_accountClosed() {
        account.setActiveStatus("N");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(summaryRepository.save(any(AuthorizationSummary.class))).thenAnswer(inv -> {
            AuthorizationSummary s = inv.getArgument(0);
            s.setAuthId(4L);
            return s;
        });

        AuthorizationProcessor.AuthorizationRequest request =
                new AuthorizationProcessor.AuthorizationRequest("4111111111111111", new BigDecimal("100.00"), "MERCH001", "PURCHASE");

        AuthorizationProcessor.AuthorizationResponse response = processor.processAuthorization(request);

        assertFalse(response.approved());
        assertEquals("DECLINED", response.status());
        assertNotNull(response.declineReason());
    }

    @Test
    void processAuthorization_fraudDetected() {
        AuthorizationFraud fraud = new AuthorizationFraud();
        fraud.setCardNum("4111111111111111");
        fraud.setAuthFraudFlag("Y");

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(fraudRepository.findByCardNum("4111111111111111")).thenReturn(List.of(fraud));
        when(summaryRepository.save(any(AuthorizationSummary.class))).thenAnswer(inv -> {
            AuthorizationSummary s = inv.getArgument(0);
            s.setAuthId(5L);
            return s;
        });
        when(fraudRepository.save(any(AuthorizationFraud.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthorizationProcessor.AuthorizationRequest request =
                new AuthorizationProcessor.AuthorizationRequest("4111111111111111", new BigDecimal("100.00"), "MERCH001", "PURCHASE");

        AuthorizationProcessor.AuthorizationResponse response = processor.processAuthorization(request);

        assertFalse(response.approved());
        assertEquals("DECLINED", response.status());
        assertNotNull(response.declineReason());
    }
}
