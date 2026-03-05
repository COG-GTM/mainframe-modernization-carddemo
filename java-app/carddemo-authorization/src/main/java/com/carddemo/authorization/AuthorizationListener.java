package com.carddemo.authorization;

import com.carddemo.authorization.AuthorizationProcessor.AuthorizationRequest;
import com.carddemo.authorization.AuthorizationProcessor.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * JMS listener for authorization requests — replaces MQ trigger + MQOPEN/MQGET.
 * Receives authorization request messages, processes them, and sends response via JmsTemplate (replaces MQPUT).
 */
@Component
public class AuthorizationListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationListener.class);

    private final AuthorizationProcessor authorizationProcessor;
    private final JmsTemplate jmsTemplate;

    public AuthorizationListener(AuthorizationProcessor authorizationProcessor,
                                  JmsTemplate jmsTemplate) {
        this.authorizationProcessor = authorizationProcessor;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "${carddemo.jms.auth-request-queue:auth.request}")
    public void onAuthorizationRequest(String message) {
        log.info("Received authorization request: {}", message);

        try {
            // Parse the incoming message (simplified format: cardNum|amount|merchantId|authType)
            String[] parts = message.split("\\|");
            if (parts.length < 4) {
                log.error("Invalid authorization request format: {}", message);
                return;
            }

            AuthorizationRequest request = new AuthorizationRequest(
                    parts[0].trim(),
                    new BigDecimal(parts[1].trim()),
                    parts[2].trim(),
                    parts[3].trim()
            );

            AuthorizationResponse response = authorizationProcessor.processAuthorization(request);

            // Send response via JMS (replaces MQPUT)
            String responseMsg = String.format("%s|%s|%s|%s|%d",
                    response.status(),
                    response.declineReason() != null ? response.declineReason() : "",
                    response.approvedAmount().toPlainString(),
                    request.cardNum(),
                    response.authId());

            jmsTemplate.convertAndSend("auth.response", responseMsg);
            log.info("Sent authorization response: {}", responseMsg);

        } catch (Exception e) {
            log.error("Error processing authorization request", e);
        }
    }
}
