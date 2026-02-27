package com.carddemo.mq;

import com.carddemo.service.AuthorizationDecisionService;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationRequest;
import com.carddemo.service.AuthorizationDecisionService.AuthorizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AuthorizationRequestListener {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationRequestListener.class);
    private final AuthorizationDecisionService authorizationDecisionService;
    private final JmsTemplate jmsTemplate;
    private final String replyQueue;

    public AuthorizationRequestListener(AuthorizationDecisionService authorizationDecisionService,
                                         JmsTemplate jmsTemplate,
                                         @Value("${carddemo.mq.reply-queue}") String replyQueue) {
        this.authorizationDecisionService = authorizationDecisionService;
        this.jmsTemplate = jmsTemplate;
        this.replyQueue = replyQueue;
    }

    @JmsListener(destination = "${carddemo.mq.request-queue}")
    public void onMessage(String csvMessage) {
        log.info("Received authorization request: {}", csvMessage);
        try {
            AuthorizationRequest request = parseCsvMessage(csvMessage);
            AuthorizationResult result = authorizationDecisionService.processAuthorization(request);
            String response = formatResponse(request, result);
            jmsTemplate.convertAndSend(replyQueue, response);
            log.info("Sent authorization response: {}", response);
        } catch (Exception e) {
            log.error("Error processing authorization request", e);
            jmsTemplate.convertAndSend(replyQueue, "ERROR," + e.getMessage());
        }
    }

    public AuthorizationRequest parseCsvMessage(String csv) {
        String[] fields = csv.split(",", -1);
        AuthorizationRequest request = new AuthorizationRequest();
        if (fields.length > 0) request.setCardNum(fields[0].trim());
        if (fields.length > 1) request.setAuthType(fields[1].trim());
        if (fields.length > 2) request.setCardExpiryDate(fields[2].trim());
        if (fields.length > 3) request.setMessageType(fields[3].trim());
        if (fields.length > 4) request.setMessageSource(fields[4].trim());
        if (fields.length > 5) request.setProcessingCode(fields[5].trim());
        if (fields.length > 6) {
            try { request.setTransactionAmt(new BigDecimal(fields[6].trim())); }
            catch (NumberFormatException e) { request.setTransactionAmt(BigDecimal.ZERO); }
        }
        if (fields.length > 7) request.setMerchantCategoryCode(fields[7].trim());
        if (fields.length > 8) request.setMerchantId(fields[8].trim());
        if (fields.length > 9) request.setMerchantName(fields[9].trim());
        if (fields.length > 10) request.setMerchantCity(fields[10].trim());
        if (fields.length > 11) request.setTransactionId(fields[11].trim());
        return request;
    }

    private String formatResponse(AuthorizationRequest request, AuthorizationResult result) {
        return String.join(",",
            request.getCardNum() != null ? request.getCardNum() : "",
            result.getRespCode(),
            result.getRespReason(),
            result.getApprovedAmt().toPlainString()
        );
    }
}
