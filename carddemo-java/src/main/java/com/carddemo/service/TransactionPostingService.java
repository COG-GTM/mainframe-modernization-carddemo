package com.carddemo.service;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionPostingService {
    private static final Logger log = LoggerFactory.getLogger(TransactionPostingService.class);
    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardAccountXrefRepository xrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tcbRepository;
    private final TransactionTemplate transactionTemplate;

    public TransactionPostingService(DailyTransactionRepository dailyTransactionRepository,
                                      TransactionRepository transactionRepository,
                                      CardAccountXrefRepository xrefRepository,
                                      AccountRepository accountRepository,
                                      TransactionCategoryBalanceRepository tcbRepository,
                                      PlatformTransactionManager transactionManager) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.xrefRepository = xrefRepository;
        this.accountRepository = accountRepository;
        this.tcbRepository = tcbRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public PostingResult postDailyTransactions() {
        List<DailyTransaction> dailyTrans = dailyTransactionRepository.findAll();
        int posted = 0;
        int rejected = 0;
        List<DailyTransaction> rejections = new ArrayList<>();

        for (DailyTransaction dt : dailyTrans) {
            try {
                transactionTemplate.executeWithoutResult(status -> postSingleTransaction(dt));
                posted++;
            } catch (Exception e) {
                log.warn("Rejected transaction {}: {}", dt.getTranId(), e.getMessage());
                rejections.add(dt);
                rejected++;
            }
        }

        log.info("Transaction posting complete: {} posted, {} rejected", posted, rejected);
        return new PostingResult(posted, rejected, rejections);
    }

    private void postSingleTransaction(DailyTransaction dt) {
        CardAccountXref xref = xrefRepository.findByCardNum(dt.getTranCardNum())
            .orElseThrow(() -> new RuntimeException("Card not in xref: " + dt.getTranCardNum()));

        Account account = accountRepository.findById(xref.getAcctId())
            .orElseThrow(() -> new RuntimeException("Account not found: " + xref.getAcctId()));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new RuntimeException("Account inactive: " + account.getAcctId());
        }

        // Update account balance
        account.setCurrBal(account.getCurrBal().add(dt.getTranAmt()));
        if (dt.getTranAmt().compareTo(BigDecimal.ZERO) > 0) {
            account.setCurrCycDebit(account.getCurrCycDebit().add(dt.getTranAmt()));
        } else {
            account.setCurrCycCredit(account.getCurrCycCredit().add(dt.getTranAmt().abs()));
        }
        accountRepository.save(account);

        // Update category balance
        TransactionCategoryBalance.TranCatBalKey key = new TransactionCategoryBalance.TranCatBalKey(
            xref.getAcctId(), dt.getTranTypeCd(), dt.getTranCatCd());
        TransactionCategoryBalance tcb = tcbRepository.findById(key).orElseGet(() -> {
            TransactionCategoryBalance n = new TransactionCategoryBalance();
            n.setAcctId(xref.getAcctId());
            n.setTranTypeCd(dt.getTranTypeCd());
            n.setTranCatCd(dt.getTranCatCd());
            n.setTranCatBal(BigDecimal.ZERO);
            return n;
        });
        tcb.setTranCatBal(tcb.getTranCatBal().add(dt.getTranAmt()));
        tcbRepository.save(tcb);

        // Copy to transaction file
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));
        Transaction tran = new Transaction();
        tran.setTranId(dt.getTranId());
        tran.setTranTypeCd(dt.getTranTypeCd());
        tran.setTranCatCd(dt.getTranCatCd());
        tran.setTranSource(dt.getTranSource());
        tran.setTranDesc(dt.getTranDesc());
        tran.setTranAmt(dt.getTranAmt());
        tran.setTranMerchantId(dt.getTranMerchantId());
        tran.setTranMerchantName(dt.getTranMerchantName());
        tran.setTranMerchantCity(dt.getTranMerchantCity());
        tran.setTranMerchantZip(dt.getTranMerchantZip());
        tran.setTranCardNum(dt.getTranCardNum());
        tran.setTranOrigTs(dt.getTranOrigTs());
        tran.setTranProcTs(now);
        transactionRepository.save(tran);
    }

    public static class PostingResult {
        private final int posted;
        private final int rejected;
        private final List<DailyTransaction> rejections;
        public PostingResult(int posted, int rejected, List<DailyTransaction> rejections) {
            this.posted = posted; this.rejected = rejected; this.rejections = rejections;
        }
        public int getPosted() { return posted; }
        public int getRejected() { return rejected; }
        public List<DailyTransaction> getRejections() { return rejections; }
    }
}
