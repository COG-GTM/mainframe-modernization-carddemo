package com.carddemo.service;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.BusinessException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class BillPaymentService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction processPayment(BillPaymentRequest request) {
        Account account = accountRepository.findById(request.getAcctId())
            .orElseThrow(() -> new BusinessException("3100", "Account not found: " + request.getAcctId()));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new BusinessException("4300", "Account is not active");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("5200", "Payment amount must be positive");
        }

        // Bill payment reduces balance (credit to account)
        BigDecimal newBal = account.getCurrBal().subtract(request.getAmount());
        account.setCurrBal(newBal);
        account.setCurrCycCredit(account.getCurrCycCredit().add(request.getAmount()));
        accountRepository.save(account);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));
        Transaction tran = new Transaction();
        tran.setTranId(UUID.randomUUID().toString().substring(0, 16));
        tran.setTranTypeCd("BP");
        tran.setTranCatCd(9999);
        tran.setTranSource("ONLINE");
        tran.setTranDesc("Bill Payment");
        tran.setTranAmt(request.getAmount().negate());
        tran.setTranOrigTs(now);
        tran.setTranProcTs(now);
        return transactionRepository.save(tran);
    }
}
