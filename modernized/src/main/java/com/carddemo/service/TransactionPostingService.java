package com.carddemo.service;

import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.DailyTransactionReject;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRejectRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CBTRN02C - POSTTRAN: posts the daily transaction file against the account master.
 *
 * <p>Each daily transaction is validated against the card cross reference and the account master;
 * valid ones update the transaction category balance, the account balance and cycle totals and are
 * written to the TRANSACT file, while invalid ones go to DALYREJS with the COBOL reason code.
 */
@Service
public class TransactionPostingService {

    public static final int REASON_INVALID_CARD = 100;
    public static final int REASON_ACCOUNT_NOT_FOUND = 101;
    public static final int REASON_OVERLIMIT = 102;
    public static final int REASON_EXPIRED = 103;

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;
    private final TransactionCategoryBalanceRepository categoryBalances;
    private final TransactionRepository transactions;
    private final DailyTransactionRejectRepository rejects;

    public TransactionPostingService(CardXrefRepository xrefs,
                                     AccountRepository accounts,
                                     TransactionCategoryBalanceRepository categoryBalances,
                                     TransactionRepository transactions,
                                     DailyTransactionRejectRepository rejects) {
        this.xrefs = xrefs;
        this.accounts = accounts;
        this.categoryBalances = categoryBalances;
        this.transactions = transactions;
        this.rejects = rejects;
    }

    /** Outcome of posting one daily transaction. */
    public record PostingResult(boolean posted, Integer reasonCode, String reasonDescription) {

        static PostingResult accepted() {
            return new PostingResult(true, null, null);
        }

        static PostingResult denied(int reasonCode, String description) {
            return new PostingResult(false, reasonCode, description);
        }
    }

    /**
     * Posts a single daily transaction. The account balance, category balance and posted
     * transaction are written in one database transaction, replacing the COBOL program's
     * sequence of unprotected VSAM updates.
     */
    @Transactional
    public PostingResult post(DailyTransaction daily) {
        Optional<CardXref> xref = xrefs.findById(daily.getCardNumber() == null ? "" : daily.getCardNumber().trim());
        if (xref.isEmpty()) {
            return reject(daily, REASON_INVALID_CARD, "INVALID CARD NUMBER FOUND");
        }

        Optional<Account> maybeAccount = accounts.findById(xref.get().getAccountId());
        if (maybeAccount.isEmpty()) {
            return reject(daily, REASON_ACCOUNT_NOT_FOUND, "ACCOUNT RECORD NOT FOUND");
        }
        Account account = maybeAccount.get();

        BigDecimal temporaryBalance = nz(account.getCurrentCycleCredit())
                .subtract(nz(account.getCurrentCycleDebit()))
                .add(nz(daily.getAmount()));
        if (nz(account.getCreditLimit()).compareTo(temporaryBalance) < 0) {
            return reject(daily, REASON_OVERLIMIT, "OVERLIMIT TRANSACTION");
        }

        if (isAfterExpiration(daily.getOriginTimestamp(), account.getExpirationDate())) {
            return reject(daily, REASON_EXPIRED, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        updateCategoryBalance(account.getId(), daily);
        updateAccount(account, daily.getAmount());
        writePostedTransaction(daily);
        return PostingResult.accepted();
    }

    private void updateCategoryBalance(Long accountId, DailyTransaction daily) {
        TransactionCategoryBalanceId id =
                new TransactionCategoryBalanceId(accountId, daily.getTypeCode(), daily.getCategoryCode());
        TransactionCategoryBalance balance = categoryBalances.findById(id)
                .orElseGet(() -> new TransactionCategoryBalance(id, BigDecimal.ZERO));
        balance.setBalance(nz(balance.getBalance()).add(nz(daily.getAmount())));
        categoryBalances.save(balance);
    }

    private void updateAccount(Account account, BigDecimal amount) {
        BigDecimal value = nz(amount);
        account.setCurrentBalance(nz(account.getCurrentBalance()).add(value));
        if (value.signum() >= 0) {
            account.setCurrentCycleCredit(nz(account.getCurrentCycleCredit()).add(value));
        } else {
            account.setCurrentCycleDebit(nz(account.getCurrentCycleDebit()).add(value));
        }
        accounts.save(account);
    }

    private void writePostedTransaction(DailyTransaction daily) {
        Transaction posted = new Transaction();
        posted.setId(daily.getId());
        posted.setTypeCode(daily.getTypeCode());
        posted.setCategoryCode(daily.getCategoryCode());
        posted.setSource(daily.getSource());
        posted.setDescription(daily.getDescription());
        posted.setAmount(daily.getAmount());
        posted.setMerchantId(daily.getMerchantId());
        posted.setMerchantName(daily.getMerchantName());
        posted.setMerchantCity(daily.getMerchantCity());
        posted.setMerchantZip(daily.getMerchantZip());
        posted.setCardNumber(daily.getCardNumber());
        posted.setOriginTimestamp(daily.getOriginTimestamp());
        posted.setProcessingTimestamp(LocalDateTime.now().format(TIMESTAMP));
        transactions.save(posted);
    }

    private PostingResult reject(DailyTransaction daily, int reasonCode, String description) {
        DailyTransactionReject reject = new DailyTransactionReject();
        reject.setTransactionId(daily.getId());
        reject.setCardNumber(daily.getCardNumber());
        reject.setAmount(daily.getAmount());
        reject.setReasonCode(reasonCode);
        reject.setReasonDescription(description);
        reject.setValidationTrailer(String.format("%04d%-76s", reasonCode, description));
        rejects.save(reject);
        return PostingResult.denied(reasonCode, description);
    }

    private static boolean isAfterExpiration(String originTimestamp, String expirationDate) {
        if (originTimestamp == null || expirationDate == null || originTimestamp.length() < 10) {
            return false;
        }
        try {
            LocalDate origin = LocalDate.parse(originTimestamp.substring(0, 10));
            return origin.isAfter(LocalDate.parse(expirationDate.trim()));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
