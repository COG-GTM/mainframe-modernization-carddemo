package com.carddemo.messaging;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JMS listener for account inquiry — translates COACCT01.cbl MQ service.
 * Option A (MQ): receives account inquiry messages, responds with account data.
 */
@Component
public class AccountQueryListener {

    private static final Logger log = LoggerFactory.getLogger(AccountQueryListener.class);

    private final AccountRepository accountRepository;
    private final JmsTemplate jmsTemplate;

    public AccountQueryListener(AccountRepository accountRepository, JmsTemplate jmsTemplate) {
        this.accountRepository = accountRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "${carddemo.jms.account-query-queue:account.query}")
    public void onAccountQuery(String message) {
        log.info("Received account query: {}", message);

        try {
            Long acctId = Long.parseLong(message.trim());
            Optional<Account> accountOpt = accountRepository.findById(acctId);

            String response;
            if (accountOpt.isPresent()) {
                Account a = accountOpt.get();
                response = String.format("FOUND|%d|%s|%s|%s",
                        a.getAcctId(),
                        a.getActiveStatus(),
                        a.getCurrentBalance().toPlainString(),
                        a.getCreditLimit().toPlainString());
            } else {
                response = "NOT_FOUND|" + acctId;
            }

            jmsTemplate.convertAndSend("account.query.response", response);
            log.info("Sent account query response: {}", response);
        } catch (Exception e) {
            log.error("Error processing account query", e);
        }
    }
}
