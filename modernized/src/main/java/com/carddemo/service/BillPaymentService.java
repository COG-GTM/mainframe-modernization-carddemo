package com.carddemo.service;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.web.dto.BillPaymentRequest;
import com.carddemo.web.dto.BillPaymentResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBIL00C - online bill payment: pays the full account balance and writes a matching transaction
 * with the fixed merchant details the COBOL program hard coded.
 */
@Service
public class BillPaymentService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TYPE_CODE = "02";
    private static final int CATEGORY_CODE = 2;
    private static final String SOURCE = "POS TERM";
    private static final String DESCRIPTION = "BILL PAYMENT - ONLINE";
    private static final long MERCHANT_ID = 999_999_999L;
    private static final String MERCHANT_NAME = "BILL PAYMENT";
    private static final String MERCHANT_CITY = "N/A";
    private static final String MERCHANT_ZIP = "N/A";

    private final AccountRepository accounts;
    private final CardXrefRepository xrefs;
    private final TransactionRepository transactions;

    public BillPaymentService(AccountRepository accounts,
                              CardXrefRepository xrefs,
                              TransactionRepository transactions) {
        this.accounts = accounts;
        this.xrefs = xrefs;
        this.transactions = transactions;
    }

    @Transactional
    public BillPaymentResponse pay(BillPaymentRequest request) {
        if (request.accountId() == null) {
            throw new BusinessRuleException("Acct ID can NOT be empty...");
        }
        AccountService.validateAccountId(request.accountId());

        String confirm = request.confirm() == null ? "" : request.confirm().trim();
        if (!confirm.equalsIgnoreCase("Y")) {
            throw new BusinessRuleException("Invalid value. Valid values are (Y/N)...");
        }

        Account account = accounts.findById(request.accountId())
                .orElseThrow(() -> new RecordNotFoundException("Account ID NOT found..."));
        CardXref xref = xrefs.findFirstByAccountIdOrderByCardNumber(account.getId())
                .orElseThrow(() -> new RecordNotFoundException("Card Xref NOT found..."));

        BigDecimal balance = account.getCurrentBalance();
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("You have nothing to pay...");
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        Transaction transaction = new Transaction();
        transaction.setId(nextTransactionId());
        transaction.setTypeCode(TYPE_CODE);
        transaction.setCategoryCode(CATEGORY_CODE);
        transaction.setSource(SOURCE);
        transaction.setDescription(DESCRIPTION);
        transaction.setAmount(balance);
        transaction.setMerchantId(MERCHANT_ID);
        transaction.setMerchantName(MERCHANT_NAME);
        transaction.setMerchantCity(MERCHANT_CITY);
        transaction.setMerchantZip(MERCHANT_ZIP);
        transaction.setCardNumber(xref.getCardNumber());
        transaction.setOriginTimestamp(timestamp);
        transaction.setProcessingTimestamp(timestamp);
        transactions.save(transaction);

        account.setCurrentBalance(balance.subtract(balance));
        accounts.save(account);

        return new BillPaymentResponse(transaction.getId(), balance, account.getCurrentBalance());
    }

    private String nextTransactionId() {
        long last = transactions.findFirstByOrderByIdDesc()
                .map(transaction -> Long.parseLong(transaction.getId().trim()))
                .orElse(0L);
        return String.format("%016d", last + 1);
    }
}
