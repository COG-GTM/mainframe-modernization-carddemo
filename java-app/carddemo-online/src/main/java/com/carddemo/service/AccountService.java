package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Account management service — replaces COACTVWC and COACTUPC.
 * Reads Account entity, fetches associated Customer and Cards via CardXref.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CardRepository cardRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.cardRepository = cardRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get account with associated customer and cards.
     * Mirrors COACTVWC: CICS READ on ACCTDAT, CUSTDAT, CARDXREF.
     */
    public Map<String, Object> getAccount(Long acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));

        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);

        Map<String, Object> result = new HashMap<>();
        result.put("account", account);

        if (!xrefs.isEmpty()) {
            Long custId = xrefs.get(0).getCustId();
            Optional<Customer> customer = customerRepository.findById(custId);
            customer.ifPresent(c -> result.put("customer", c));

            List<Card> cards = cardRepository.findByAcctId(acctId);
            result.put("cards", cards);
        }

        return result;
    }

    /**
     * Update account fields.
     * Mirrors COACTUPC: CICS REWRITE on ACCTDAT with field validation.
     */
    @Transactional
    public Account updateAccount(Long acctId, AccountUpdateRequest dto) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));

        if (dto.getActiveStatus() != null) account.setActiveStatus(dto.getActiveStatus());
        if (dto.getCreditLimit() != null) account.setCreditLimit(dto.getCreditLimit());
        if (dto.getCashCreditLimit() != null) account.setCashCreditLimit(dto.getCashCreditLimit());
        if (dto.getOpenDate() != null) account.setOpenDate(LocalDate.parse(dto.getOpenDate()));
        if (dto.getExpirationDate() != null) account.setExpirationDate(LocalDate.parse(dto.getExpirationDate()));
        if (dto.getReissueDate() != null) account.setReissueDate(LocalDate.parse(dto.getReissueDate()));
        if (dto.getAddrZip() != null) account.setAddrZip(dto.getAddrZip());
        if (dto.getGroupId() != null) account.setGroupId(dto.getGroupId());

        return accountRepository.save(account);
    }
}
