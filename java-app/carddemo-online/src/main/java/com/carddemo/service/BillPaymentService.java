package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bill payment service — replaces COBIL00C.cbl.
 * Pays account balance in full, creates a payment transaction record.
 * Uses @Transactional for atomicity (replaces CICS SYNCPOINT).
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DailyTransactionRepository dailyTransactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                               CardXrefRepository cardXrefRepository,
                               DailyTransactionRepository dailyTransactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
    }

    /**
     * Pay account balance in full.
     * Mirrors COBIL00C: pays current balance, creates payment transaction.
     */
    @Transactional
    public DailyTransaction payBill(Long acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));

        BigDecimal balance = account.getCurrentBalance();
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No balance to pay for account: " + acctId);
        }

        // Find the first card for this account to associate the payment
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        if (xrefs.isEmpty()) {
            throw new IllegalStateException("No card found for account: " + acctId);
        }

        String cardNum = xrefs.get(0).getCardNum();

        // Create payment transaction record
        DailyTransaction payment = new DailyTransaction();
        payment.setTransactionId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        payment.setCardNum(cardNum);
        payment.setTypeCd("02"); // Payment type
        payment.setCategoryCd(1);
        payment.setSource("ONLINE");
        payment.setDescription("Bill Payment - Full Balance");
        payment.setAmount(balance);
        payment.setOrigTimestamp(LocalDateTime.now());

        // Update account balance
        account.setCurrentBalance(BigDecimal.ZERO);
        accountRepository.save(account);

        return dailyTransactionRepository.save(payment);
    }
}
