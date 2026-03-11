package com.carddemo.service;

import com.carddemo.mq.AuthorizationRequestListener;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationRequest;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationRequestListenerTest {
    @Mock private AuthorizationDecisionService authService;
    @Mock private JmsTemplate jmsTemplate;

    @Test
    void parseCsvMessage() {
        AuthorizationRequestListener listener = new AuthorizationRequestListener(authService, jmsTemplate, "REPLY_Q");
        AuthorizationRequest request = listener.parseCsvMessage("4111111111111111,AUTH,1230,0100,ONLINE,003000,100.00,5411,MERCH001,TEST STORE,NEW YORK,TXN001");
        assertEquals("4111111111111111", request.getCardNum());
        assertEquals("AUTH", request.getAuthType());
        assertEquals("1230", request.getCardExpiryDate());
        assertEquals(new BigDecimal("100.00"), request.getTransactionAmt());
        assertEquals("MERCH001", request.getMerchantId());
    }

    @Test
    void onMessage_approved() {
        AuthorizationRequestListener listener = new AuthorizationRequestListener(authService, jmsTemplate, "REPLY_Q");
        when(authService.processAuthorization(any())).thenReturn(
            new AuthorizationResult("00", "0000", new BigDecimal("100.00"), 1L));
        listener.onMessage("4111111111111111,AUTH,1230,0100,ONLINE,003000,100.00,5411,MERCH001,TEST STORE,NEW YORK,TXN001");
        verify(jmsTemplate).convertAndSend(eq("REPLY_Q"), contains("4111111111111111"));
    }

    @Test
    void onMessage_declined() {
        AuthorizationRequestListener listener = new AuthorizationRequestListener(authService, jmsTemplate, "REPLY_Q");
        when(authService.processAuthorization(any())).thenReturn(
            new AuthorizationResult("05", "3100", BigDecimal.ZERO, 1L));
        listener.onMessage("0000000000000000,AUTH,1230,0100,ONLINE,003000,100.00,5411,MERCH001,TEST STORE,NEW YORK,TXN001");
        verify(jmsTemplate).convertAndSend(eq("REPLY_Q"), contains("0000000000000000"));
    }
}
