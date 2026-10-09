package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.domain.Account;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRejectRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionPostingService.PostingResult;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TransactionPostingServiceTest extends AbstractIntegrationTest {

    private static final String CARD = "4444333322221111";

    @Autowired
    private TransactionPostingService posting;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private CardXrefRepository xrefs;
    @Autowired
    private TransactionRepository transactions;
    @Autowired
    private DailyTransactionRejectRepository rejects;
    @Autowired
    private TransactionCategoryBalanceRepository categoryBalances;

    @BeforeEach
    void reset() {
        rejects.deleteAll();
        transactions.deleteAll();
        categoryBalances.deleteAll();
        xrefs.deleteAll();
        accounts.deleteAll();

        Account account = new Account();
        account.setId(700000001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("100.00"));
        account.setCreditLimit(new BigDecimal("1000.00"));
        account.setCashCreditLimit(new BigDecimal("500.00"));
        account.setCurrentCycleCredit(new BigDecimal("0.00"));
        account.setCurrentCycleDebit(new BigDecimal("0.00"));
        account.setOpenDate("2020-01-01");
        account.setExpirationDate("2030-01-01");
        account.setGroupId("DEFAULT");
        accounts.save(account);

        CardXref xref = new CardXref();
        xref.setCardNumber(CARD);
        xref.setAccountId(account.getId());
        xref.setCustomerId(1L);
        xrefs.save(xref);
    }

    private DailyTransaction daily(String id, String cardNumber, String amount, String originTimestamp) {
        DailyTransaction daily = new DailyTransaction();
        daily.setId(id);
        daily.setTypeCode("01");
        daily.setCategoryCode(1);
        daily.setSource("POS TERM");
        daily.setDescription("Test purchase");
        daily.setAmount(new BigDecimal(amount));
        daily.setMerchantId(999999999L);
        daily.setMerchantName("TEST MERCHANT");
        daily.setMerchantCity("TEST CITY");
        daily.setMerchantZip("12345");
        daily.setCardNumber(cardNumber);
        daily.setOriginTimestamp(originTimestamp);
        daily.setProcessingTimestamp(originTimestamp);
        return daily;
    }

    @Test
    void postsValidTransactionAtomically() {
        PostingResult result = posting.post(daily("0000000000000001", CARD, "150.00", "2024-05-01 10:00:00.000000"));

        assertThat(result.posted()).isTrue();
        Account account = accounts.findById(700000001L).orElseThrow();
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("250.00");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("150.00");
        assertThat(transactions.findById("0000000000000001")).isPresent();
        assertThat(categoryBalances.findByIdAccountIdOrderByIdTypeCodeAscIdCategoryCodeAsc(700000001L))
                .singleElement()
                .satisfies(balance -> assertThat(balance.getBalance()).isEqualByComparingTo("150.00"));
        assertThat(rejects.count()).isZero();
    }

    @Test
    void creditsDebitBucketForRefunds() {
        posting.post(daily("0000000000000002", CARD, "-25.00", "2024-05-01 10:00:00.000000"));

        Account account = accounts.findById(700000001L).orElseThrow();
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("75.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("-25.00");
    }

    @Test
    void rejectsUnknownCard() {
        PostingResult result = posting.post(
                daily("0000000000000003", "1111222233334444", "10.00", "2024-05-01 10:00:00.000000"));

        assertThat(result.posted()).isFalse();
        assertThat(result.reasonCode()).isEqualTo(TransactionPostingService.REASON_INVALID_CARD);
        assertThat(result.reasonDescription()).isEqualTo("INVALID CARD NUMBER FOUND");
        assertThat(rejects.count()).isEqualTo(1);
        assertThat(transactions.count()).isZero();
    }

    @Test
    void rejectsOverlimitTransaction() {
        PostingResult result = posting.post(daily("0000000000000004", CARD, "5000.00", "2024-05-01 10:00:00.000000"));

        assertThat(result.reasonCode()).isEqualTo(TransactionPostingService.REASON_OVERLIMIT);
        assertThat(accounts.findById(700000001L).orElseThrow().getCurrentBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejectsTransactionAfterAccountExpiration() {
        PostingResult result = posting.post(daily("0000000000000005", CARD, "10.00", "2031-05-01 10:00:00.000000"));

        assertThat(result.reasonCode()).isEqualTo(TransactionPostingService.REASON_EXPIRED);
        assertThat(result.reasonDescription()).isEqualTo("TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
    }
}
