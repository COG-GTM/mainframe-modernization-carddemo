package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardAccountXref;
import com.carddemo.entity.AuthSummary;
import com.carddemo.entity.AuthDetail;
import com.carddemo.repository.*;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationRequest;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationDecisionServiceTest {
    @Mock private CardAccountXrefRepository xrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AuthSummaryRepository authSummaryRepository;
    @Mock private AuthDetailRepository authDetailRepository;
    @InjectMocks private AuthorizationDecisionService service;
    private CardAccountXref xref;
    private Account account;

    @BeforeEach
    void setUp() {
        xref = new CardAccountXref();
        xref.setCardNum("4111111111111111");
        xref.setAcctId(10000000001L);
        xref.setCustId(100000001L);

        account = new Account();
        account.setAcctId(10000000001L);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("1500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));

        when(authSummaryRepository.save(any(AuthSummary.class))).thenAnswer(i -> {
            AuthSummary s = i.getArgument(0);
            s.setId(1L);
            return s;
        });
        lenient().when(authDetailRepository.save(any(AuthDetail.class))).thenAnswer(i -> i.getArgument(0));
    }

    private AuthorizationRequest createRequest(String cardNum, BigDecimal amount) {
        AuthorizationRequest req = new AuthorizationRequest();
        req.setCardNum(cardNum);
        req.setAuthType("AUTH");
        req.setCardExpiryDate("1230");
        req.setTransactionAmt(amount);
        req.setMerchantId("MERCHANT1");
        req.setMerchantName("TEST STORE");
        return req;
    }

    @Test
    void processAuth_approved() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        AuthorizationResult result = service.processAuthorization(createRequest("4111111111111111", new BigDecimal("100.00")));
        assertTrue(result.isApproved());
        assertEquals("00", result.getRespCode());
        assertEquals("0000", result.getRespReason());
    }

    @Test
    void processAuth_cardNotFound_3100() {
        when(xrefRepository.findByCardNum("0000000000000000")).thenReturn(Optional.empty());
        AuthorizationResult result = service.processAuthorization(createRequest("0000000000000000", new BigDecimal("100.00")));
        assertFalse(result.isApproved());
        assertEquals("3100", result.getRespReason());
    }

    @Test
    void processAuth_accountNotFound_4100() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.empty());
        AuthorizationResult result = service.processAuthorization(createRequest("4111111111111111", new BigDecimal("100.00")));
        assertFalse(result.isApproved());
        assertEquals("4100", result.getRespReason());
    }

    @Test
    void processAuth_accountInactive_4200() {
        account.setActiveStatus("N");
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        AuthorizationResult result = service.processAuthorization(createRequest("4111111111111111", new BigDecimal("100.00")));
        assertFalse(result.isApproved());
        assertEquals("4200", result.getRespReason());
    }

    @Test
    void processAuth_exceedsCreditLimit_4300() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        AuthorizationResult result = service.processAuthorization(createRequest("4111111111111111", new BigDecimal("9000.00")));
        assertFalse(result.isApproved());
        assertEquals("4300", result.getRespReason());
    }

    @Test
    void processAuth_cardExpired_5100() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        AuthorizationRequest req = createRequest("4111111111111111", new BigDecimal("100.00"));
        req.setCardExpiryDate("0120"); // Jan 2020 - expired
        AuthorizationResult result = service.processAuthorization(req);
        assertFalse(result.isApproved());
        assertEquals("5100", result.getRespReason());
    }
}
