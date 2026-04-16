package com.cardemo.batch.service;

import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Implements paragraph 2700-UPDATE-TCATBAL from CBTRN02C.
 * Manages transaction category balance create/update logic.
 */
@Service
public class TransactionCategoryBalanceService {

    private static final Logger log = LoggerFactory.getLogger(TransactionCategoryBalanceService.class);

    private final TransactionCategoryBalanceRepository repository;

    public TransactionCategoryBalanceService(TransactionCategoryBalanceRepository repository) {
        this.repository = repository;
    }

    /**
     * Result of updating the category balance, includes whether a new record was created.
     */
    public record UpdateResult(TransactionCategoryBalance categoryBalance, boolean created) {
    }

    /**
     * Updates or creates a transaction category balance record.
     *
     * Paragraph 2700 logic:
     * - Lookup TCATBAL by composite key (ACCT-ID, TYPE-CD, CAT-CD)
     * - If not found (status '23'), create new record with INITIALIZE then set fields
     * - If found, ADD DALYTRAN-AMT TO TRAN-CAT-BAL and REWRITE
     */
    public UpdateResult updateCategoryBalance(long acctId, DailyTransaction dailyTran) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(
                acctId, dailyTran.getTypeCd(), dailyTran.getCatCd());

        Optional<TransactionCategoryBalance> existing = repository.findById(id);

        if (existing.isPresent()) {
            // 2700-B-UPDATE-TCATBAL-REC: ADD DALYTRAN-AMT TO TRAN-CAT-BAL
            TransactionCategoryBalance catBal = existing.get();
            catBal.setBalance(catBal.getBalance().add(dailyTran.getAmount()));
            return new UpdateResult(catBal, false);
        } else {
            // 2700-A-CREATE-TCATBAL-REC: INITIALIZE, set fields, add amount
            log.debug("TCATBAL record not found for key: {}/{}/{} .. Creating.",
                    acctId, dailyTran.getTypeCd(), dailyTran.getCatCd());
            TransactionCategoryBalance newCatBal = new TransactionCategoryBalance();
            newCatBal.setAcctId(acctId);
            newCatBal.setTypeCd(dailyTran.getTypeCd());
            newCatBal.setCatCd(dailyTran.getCatCd());
            newCatBal.setBalance(BigDecimal.ZERO.add(dailyTran.getAmount()));
            return new UpdateResult(newCatBal, true);
        }
    }
}
