package com.carddemo.service;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.CardAccountXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.exception.BusinessException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardAccountXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardAccountXrefRepository xrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tcbRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardAccountXrefRepository xrefRepository,
                              AccountRepository accountRepository,
                              TransactionCategoryBalanceRepository tcbRepository) {
        this.transactionRepository = transactionRepository;
        this.xrefRepository = xrefRepository;
        this.accountRepository = accountRepository;
        this.tcbRepository = tcbRepository;
    }

    public Page<Transaction> listTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Transaction getTransaction(String id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        CardAccountXref xref = xrefRepository.findByCardNum(request.getCardNum())
            .orElseThrow(() -> new BusinessException("4100", "Card not found in cross-reference"));

        Account account = accountRepository.findById(xref.getAcctId())
            .orElseThrow(() -> new BusinessException("4200", "Account not found for card"));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new BusinessException("4300", "Account is not active");
        }

        BigDecimal newBal = account.getCurrBal().add(request.getTranAmt());
        if (newBal.compareTo(account.getCreditLimit()) > 0) {
            throw new BusinessException("5100", "Transaction exceeds credit limit");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));
        Transaction tran = new Transaction();
        tran.setTranId(UUID.randomUUID().toString().substring(0, 16));
        tran.setTranTypeCd(request.getTranTypeCd());
        tran.setTranCatCd(request.getTranCatCd());
        tran.setTranDesc(request.getTranDesc());
        tran.setTranAmt(request.getTranAmt());
        tran.setTranCardNum(request.getCardNum());
        tran.setTranMerchantId(request.getMerchantId());
        tran.setTranMerchantName(request.getMerchantName());
        tran.setTranMerchantCity(request.getMerchantCity());
        tran.setTranMerchantZip(request.getMerchantZip());
        tran.setTranOrigTs(now);
        tran.setTranProcTs(now);
        tran.setTranSource("ONLINE");

        account.setCurrBal(newBal);
        if (request.getTranAmt().compareTo(BigDecimal.ZERO) > 0) {
            account.setCurrCycDebit(account.getCurrCycDebit().add(request.getTranAmt()));
        } else {
            account.setCurrCycCredit(account.getCurrCycCredit().add(request.getTranAmt().abs()));
        }
        accountRepository.save(account);

        updateCategoryBalance(xref.getAcctId(), request.getTranTypeCd(), request.getTranCatCd(), request.getTranAmt());

        return transactionRepository.save(tran);
    }

    private void updateCategoryBalance(Long acctId, String typeCd, Integer catCd, BigDecimal amount) {
        TransactionCategoryBalance.TranCatBalKey key = new TransactionCategoryBalance.TranCatBalKey(acctId, typeCd, catCd);
        TransactionCategoryBalance tcb = tcbRepository.findById(key).orElseGet(() -> {
            TransactionCategoryBalance n = new TransactionCategoryBalance();
            n.setAcctId(acctId);
            n.setTranTypeCd(typeCd);
            n.setTranCatCd(catCd);
            n.setTranCatBal(BigDecimal.ZERO);
            return n;
        });
        tcb.setTranCatBal(tcb.getTranCatBal().add(amount));
        tcbRepository.save(tcb);
    }
}
