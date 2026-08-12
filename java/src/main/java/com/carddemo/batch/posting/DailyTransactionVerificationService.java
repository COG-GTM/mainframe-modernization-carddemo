package com.carddemo.batch.posting;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: CBTRN01C — daily transaction file verification pass.
 *
 * <p>Files and copybooks: DALYTRAN (CVTRA06Y), CARDXREF (CVACT03Y), ACCTDATA (CVACT01Y),
 * CUSTDATA (CVCUS01Y), CARDDATA (CVACT02Y) and TRANSACT (CVTRA05Y) — the last three are only
 * opened and closed by the program. The program updates nothing: it reads DALYTRAN sequentially
 * and, per record, looks the card up in the cross-reference (2000-LOOKUP-XREF) and then reads the
 * account (3000-READ-ACCOUNT), reporting the records that cannot be verified.
 */
@Service
public class DailyTransactionVerificationService {

    private static final Logger log = LoggerFactory.getLogger(DailyTransactionVerificationService.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public DailyTransactionVerificationService(DailyTransactionRepository dailyTransactionRepository,
                                               CardXrefRepository cardXrefRepository,
                                               AccountRepository accountRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    /** MAIN-PARA: the read loop over the whole DALYTRAN file. */
    @Transactional(readOnly = true)
    public VerificationSummary verifyAll() {
        List<DailyTransaction> transactions = dailyTransactionRepository.findAll();
        int cardsNotFound = 0;
        int accountsNotFound = 0;
        for (DailyTransaction transaction : transactions) {
            switch (verify(transaction)) {
                case CARD_NOT_VERIFIED -> cardsNotFound++;
                case ACCOUNT_NOT_FOUND -> accountsNotFound++;
                default -> { }
            }
        }
        return new VerificationSummary(transactions.size(), cardsNotFound, accountsNotFound);
    }

    /** 2000-LOOKUP-XREF followed by 3000-READ-ACCOUNT for a single DALYTRAN record. */
    @Transactional(readOnly = true)
    public VerificationStatus verify(DailyTransaction transaction) {
        String cardNumber = transaction.getCardNumber() == null ? "" : transaction.getCardNumber().trim();
        Optional<CardXref> xref = cardXrefRepository.findById(cardNumber);
        if (xref.isEmpty()) {
            log.info("CARD NUMBER {} COULD NOT BE VERIFIED. SKIPPING TRANSACTION ID-{}",
                    transaction.getCardNumber(), transaction.getTransactionId());
            return VerificationStatus.CARD_NOT_VERIFIED;
        }
        Long accountId = xref.get().getAccountId();
        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isEmpty()) {
            log.info("ACCOUNT {} NOT FOUND", accountId);
            return VerificationStatus.ACCOUNT_NOT_FOUND;
        }
        return VerificationStatus.VERIFIED;
    }

    /** Outcome of the two lookups performed for one DALYTRAN record. */
    public enum VerificationStatus {
        /** Cross-reference and account were both read successfully. */
        VERIFIED,
        /** 2000-LOOKUP-XREF hit INVALID KEY. */
        CARD_NOT_VERIFIED,
        /** 3000-READ-ACCOUNT hit INVALID KEY. */
        ACCOUNT_NOT_FOUND
    }

    /** Counters reported for the whole DALYTRAN file. */
    public record VerificationSummary(int transactionsRead, int cardsNotVerified, int accountsNotFound) {
    }
}
