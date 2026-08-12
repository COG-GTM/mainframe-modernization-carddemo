package com.carddemo.batch.posting;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.batch.posting.DailyTransactionVerificationService.VerificationStatus;
import com.carddemo.batch.posting.DailyTransactionVerificationService.VerificationSummary;
import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** COBOL program: CBTRN01C — cross-reference and account verification of the DALYTRAN file. */
@DataJpaTest
class DailyTransactionVerificationServiceTest {

    private static final String KNOWN_CARD = "4859452612877065";
    private static final String CARD_WITHOUT_ACCOUNT = "4859452612877066";
    private static final String UNKNOWN_CARD = "9999999999999999";

    @Autowired
    private DailyTransactionRepository dailyTransactions;
    @Autowired
    private CardXrefRepository xrefs;
    @Autowired
    private AccountRepository accounts;

    private DailyTransactionVerificationService service;

    @BeforeEach
    void setUp() {
        service = new DailyTransactionVerificationService(dailyTransactions, xrefs, accounts);
        xrefs.save(CardXref.builder().cardNumber(KNOWN_CARD).customerId(9L).accountId(11L).build());
        xrefs.save(CardXref.builder().cardNumber(CARD_WITHOUT_ACCOUNT).customerId(9L).accountId(99L).build());
        accounts.save(Account.builder()
                .accountId(11L)
                .activeStatus("Y")
                .currentBalance(new BigDecimal("500.00"))
                .creditLimit(new BigDecimal("2000.00"))
                .currentCycleCredit(BigDecimal.ZERO)
                .currentCycleDebit(BigDecimal.ZERO)
                .expirationDate("2030-12-31")
                .build());
    }

    private DailyTransaction save(String id, String cardNumber) {
        return dailyTransactions.save(DailyTransaction.builder()
                .transactionId(id)
                .typeCode("01")
                .categoryCode(1)
                .amount(new BigDecimal("10.00"))
                .cardNumber(cardNumber)
                .originTimestamp("2022-06-10 19:27:53.000000")
                .build());
    }

    @Test
    void classifiesEachRecordByItsLookups() {
        assertThat(service.verify(save("0000000000000001", KNOWN_CARD)))
                .isEqualTo(VerificationStatus.VERIFIED);
        assertThat(service.verify(save("0000000000000002", UNKNOWN_CARD)))
                .isEqualTo(VerificationStatus.CARD_NOT_VERIFIED);
        assertThat(service.verify(save("0000000000000003", CARD_WITHOUT_ACCOUNT)))
                .isEqualTo(VerificationStatus.ACCOUNT_NOT_FOUND);
    }

    @Test
    void countsTheWholeFile() {
        save("0000000000000001", KNOWN_CARD);
        save("0000000000000002", KNOWN_CARD);
        save("0000000000000003", UNKNOWN_CARD);
        save("0000000000000004", CARD_WITHOUT_ACCOUNT);

        VerificationSummary summary = service.verifyAll();

        assertThat(summary.transactionsRead()).isEqualTo(4);
        assertThat(summary.cardsNotVerified()).isEqualTo(1);
        assertThat(summary.accountsNotFound()).isEqualTo(1);
    }
}
