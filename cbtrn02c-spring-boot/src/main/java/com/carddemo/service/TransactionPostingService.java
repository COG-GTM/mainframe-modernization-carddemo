package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.TranCatBalanceId;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionReject;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRejectRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Java port of the COBOL batch program CBTRN02C: validates daily transactions against the
 * card cross-reference and account master, posts valid transactions (updating category
 * balances and account balances), and records rejects with a fail reason.
 */
@Service
public class TransactionPostingService {

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;
    private final TransactionRejectRepository transactionRejectRepository;

    /**
     * Self-reference (the Spring proxy) so {@link #processAllTransactions} routes calls to
     * {@link #processSingleTransaction} through the proxy and its {@code @Transactional} advice
     * is honored. A direct {@code this.} call would be a self-invocation and bypass the proxy.
     */
    @Autowired
    @Lazy
    private TransactionPostingService self;

    public TransactionPostingService(CardXrefRepository cardXrefRepository,
                                     AccountRepository accountRepository,
                                     TransactionRepository transactionRepository,
                                     TranCatBalanceRepository tranCatBalanceRepository,
                                     TransactionRejectRepository transactionRejectRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.transactionRejectRepository = transactionRejectRepository;
    }

    /**
     * Process every transaction in the batch. Mirrors the main PERFORM UNTIL loop in CBTRN02C.
     */
    public BatchResult processAllTransactions(List<DailyTransaction> transactions) {
        long transactionCount = 0;
        long rejectCount = 0;
        for (DailyTransaction txn : transactions) {
            transactionCount++;
            ValidationResult result = self.processSingleTransaction(txn);
            if (result.isRejected()) {
                rejectCount++;
            }
        }
        return new BatchResult(transactionCount, rejectCount);
    }

    /**
     * Validate a single transaction and, if valid, post it. Equivalent to the COBOL
     * 1500-VALIDATE-TRAN followed by either 2000-POST-TRANSACTION or 2500-WRITE-REJECT-REC.
     */
    @Transactional
    public ValidationResult processSingleTransaction(DailyTransaction txn) {
        ValidationResult result = validate(txn);
        if (result.isValid()) {
            post(txn);
        } else {
            writeReject(txn, result);
        }
        return result;
    }

    private ValidationResult validate(DailyTransaction txn) {
        // 1500-A-LOOKUP-XREF
        Optional<CardXref> xrefOpt = cardXrefRepository.findByCardNum(txn.getCardNum());
        if (xrefOpt.isEmpty()) {
            return ValidationResult.rejected(100, "INVALID CARD NUMBER FOUND");
        }
        CardXref xref = xrefOpt.get();

        // 1500-B-LOOKUP-ACCT
        Optional<Account> accountOpt = accountRepository.findByAcctId(xref.getAcctId());
        if (accountOpt.isEmpty()) {
            return ValidationResult.rejected(101, "ACCOUNT RECORD NOT FOUND");
        }
        Account account = accountOpt.get();

        // In the COBOL, the credit-limit check (102) and the expiration check (103) run
        // independently and sequentially; the expiration check can OVERWRITE the
        // credit-limit rejection (last failure code wins).
        int failCode = 0;
        String failDesc = "";

        // Credit-limit check (102): tempBal = currCycCredit - currCycDebit + amount.
        BigDecimal tempBal = nz(account.getCurrCycCredit())
                .subtract(nz(account.getCurrCycDebit()))
                .add(nz(txn.getAmount()));
        if (nz(account.getCreditLimit()).compareTo(tempBal) < 0) {
            failCode = 102;
            failDesc = "OVERLIMIT TRANSACTION";
        }

        // Expiration check (103): account expired when expirationDate < origTimestamp(1:10).
        if (isExpired(account, txn)) {
            failCode = 103;
            failDesc = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";
        }

        if (failCode != 0) {
            return ValidationResult.rejected(failCode, failDesc);
        }
        return ValidationResult.ok();
    }

    private boolean isExpired(Account account, DailyTransaction txn) {
        LocalDate origDate = LocalDate.parse(txn.getOrigTimestamp().substring(0, 10));
        // COBOL passes the check when ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10);
        // it is expired (reject) when expirationDate is strictly before the origination date.
        return account.getExpirationDate().isBefore(origDate);
    }

    private void post(DailyTransaction txn) {
        // The xref/account were already validated; re-fetch within the posting transaction.
        CardXref xref = cardXrefRepository.findByCardNum(txn.getCardNum())
                .orElseThrow(() -> new BatchProcessingException(
                        "XREF disappeared during posting for card " + txn.getCardNum()));
        Account account = accountRepository.findByAcctId(xref.getAcctId())
                .orElseThrow(() -> new BatchProcessingException(
                        "Account disappeared during posting for acct " + xref.getAcctId()));

        updateTranCatBalance(xref.getAcctId(), txn);
        updateAccount(account, txn);
        writeTransaction(txn);
    }

    // 2700-UPDATE-TCATBAL
    private void updateTranCatBalance(long acctId, DailyTransaction txn) {
        Optional<TranCatBalance> existing = tranCatBalanceRepository
                .findByAcctIdAndTypeCdAndCatCd(acctId, txn.getTypeCd(), txn.getCatCd());
        if (existing.isPresent()) {
            TranCatBalance bal = existing.get();
            bal.setBalance(nz(bal.getBalance()).add(nz(txn.getAmount())));
            tranCatBalanceRepository.save(bal);
        } else {
            TranCatBalanceId id = new TranCatBalanceId(acctId, txn.getTypeCd(), txn.getCatCd());
            tranCatBalanceRepository.save(new TranCatBalance(id, nz(txn.getAmount())));
        }
    }

    // 2800-UPDATE-ACCOUNT-REC
    private void updateAccount(Account account, DailyTransaction txn) {
        BigDecimal amount = nz(txn.getAmount());
        account.setCurrBal(nz(account.getCurrBal()).add(amount));
        if (amount.signum() >= 0) {
            account.setCurrCycCredit(nz(account.getCurrCycCredit()).add(amount));
        } else {
            account.setCurrCycDebit(nz(account.getCurrCycDebit()).add(amount));
        }
        accountRepository.save(account);
    }

    // 2900-WRITE-TRANSACTION-FILE
    private void writeTransaction(DailyTransaction txn) {
        Transaction tran = new Transaction();
        tran.setId(txn.getId());
        tran.setTypeCd(txn.getTypeCd());
        tran.setCatCd(txn.getCatCd());
        tran.setSource(txn.getSource());
        tran.setDescription(txn.getDescription());
        tran.setAmount(txn.getAmount());
        tran.setMerchantId(txn.getMerchantId());
        tran.setMerchantName(txn.getMerchantName());
        tran.setMerchantCity(txn.getMerchantCity());
        tran.setMerchantZip(txn.getMerchantZip());
        tran.setCardNum(txn.getCardNum());
        tran.setOrigTimestamp(txn.getOrigTimestamp());
        tran.setProcTimestamp(Db2TimestampFormatter.now());
        transactionRepository.save(tran);
    }

    // 2500-WRITE-REJECT-REC
    private void writeReject(DailyTransaction txn, ValidationResult result) {
        TransactionReject reject = new TransactionReject(
                buildRawRecord(txn), result.getFailReasonCode(), result.getFailReasonDesc());
        transactionRejectRepository.save(reject);
    }

    /**
     * Reconstruct the fixed-width 350-byte DALYTRAN-RECORD (CVTRA06Y) for the reject file,
     * mirroring MOVE DALYTRAN-RECORD TO REJECT-TRAN-DATA.
     */
    private String buildRawRecord(DailyTransaction txn) {
        StringBuilder sb = new StringBuilder(350);
        sb.append(alpha(txn.getId(), 16));
        sb.append(alpha(txn.getTypeCd(), 2));
        sb.append(num(txn.getCatCd(), 4));
        sb.append(alpha(txn.getSource(), 10));
        sb.append(alpha(txn.getDescription(), 100));
        sb.append(amount(txn.getAmount(), 11));
        sb.append(num(txn.getMerchantId(), 9));
        sb.append(alpha(txn.getMerchantName(), 50));
        sb.append(alpha(txn.getMerchantCity(), 50));
        sb.append(alpha(txn.getMerchantZip(), 10));
        sb.append(alpha(txn.getCardNum(), 16));
        sb.append(alpha(txn.getOrigTimestamp(), 26));
        sb.append(alpha(txn.getProcTimestamp(), 26));
        sb.append(" ".repeat(20)); // FILLER X(20)
        String raw = sb.toString();
        return raw.length() > 350 ? raw.substring(0, 350) : raw;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String alpha(String value, int width) {
        String v = value == null ? "" : value;
        if (v.length() > width) {
            return v.substring(0, width);
        }
        return v + " ".repeat(width - v.length());
    }

    private static String num(long value, int width) {
        String v = Long.toString(Math.abs(value));
        if (v.length() > width) {
            return v.substring(v.length() - width);
        }
        return "0".repeat(width - v.length()) + v;
    }

    private static String amount(BigDecimal value, int width) {
        BigDecimal v = nz(value).movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).abs();
        String s = v.toPlainString();
        if (s.length() > width) {
            return s.substring(s.length() - width);
        }
        return "0".repeat(width - s.length()) + s;
    }
}
