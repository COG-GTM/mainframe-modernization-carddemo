package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.exception.ConcurrentUpdateException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.loader.LegacyDataLoader;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.web.dto.AccountUpdateRequest;
import com.carddemo.web.dto.AccountView;
import com.carddemo.web.dto.CardSummary;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuditRegressionTest extends AbstractIntegrationTest {

    @Autowired
    private LegacyDataLoader loader;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private CardService cardService;
    @Autowired
    private CardRepository cards;
    @Autowired
    private CardXrefRepository xrefs;
    @Autowired
    private TransactionRepository transactions;
    @Autowired
    private TransactionIdGenerator idGenerator;
    @Autowired
    private StatementService statements;

    @BeforeEach
    void seed() {
        loader.loadAll();
    }

    @Test
    void everySeededAccountSurvivesGetToPutRoundTrip() {
        for (Account account : accounts.findAll()) {
            AccountView view = accountService.view(account.getId());
            String status = "Y".equals(view.activeStatus()) ? "N" : "Y";
            AccountView updated = accountService.update(view.accountId(), request(view, status, view.version()));
            assertThat(updated.activeStatus()).isEqualTo(status);
        }
    }

    @Test
    void staleAccountVersionIsRejected() {
        AccountView view = accountService.view(1L);
        accountService.update(1L, request(view, "N".equals(view.activeStatus()) ? "Y" : "N", view.version()));
        accounts.flush();

        assertThatThrownBy(() -> accountService.update(1L, request(view, view.activeStatus(), view.version())))
                .isInstanceOf(ConcurrentUpdateException.class);
        assertThatThrownBy(() -> accountService.update(1L, request(view, view.activeStatus(), null)))
                .isInstanceOf(ConcurrentUpdateException.class);
    }

    @Test
    void cardNumberSearchHonoursAccountFilterAndMissingCards() {
        Card card = cards.findById("0500024453765740").orElseThrow();

        assertThat(cardService.list(card.getAccountId(), card.getCardNumber(), 0, 10).items())
                .extracting(CardSummary::cardNumber)
                .containsExactly(card.getCardNumber());
        assertThatThrownBy(() -> cardService.list(card.getAccountId() + 1, card.getCardNumber(), 0, 10))
                .isInstanceOf(RecordNotFoundException.class);
        assertThatThrownBy(() -> cardService.list(null, "9999999999999999", 0, 10))
                .isInstanceOf(RecordNotFoundException.class);
        assertThatThrownBy(() -> cardService.list(null, card.getCardNumber(), 1, 10))
                .isInstanceOf(RecordNotFoundException.class);
    }

    @Test
    void transactionIdsAreUniqueBeforeFlush() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            Transaction transaction = new Transaction();
            transaction.setId(idGenerator.next());
            transaction.setCardNumber(xrefs.findAll().get(0).getCardNumber());
            transaction.setAmount(BigDecimal.ONE);
            transaction.setProcessingTimestamp("2026-01-01 00:00:00.000000");
            transactions.save(transaction);
            ids.add(transaction.getId());
        }
        assertThat(ids).hasSize(3);
    }

    @Test
    void statementHtmlEscapesTransactionId() {
        CardXref xref = xrefs.findAll().get(0);
        Transaction transaction = new Transaction();
        transaction.setId("<b>000000001</b>");
        transaction.setCardNumber(xref.getCardNumber());
        transaction.setDescription("x");
        transaction.setAmount(BigDecimal.TEN);
        transactions.save(transaction);

        String html = statements.generate(xref).html();
        assertThat(html).doesNotContain("<b>000000001</b>").contains("&lt;b&gt;000000001&lt;/b&gt;");
    }

    private static AccountUpdateRequest request(AccountView view, String status, Long version) {
        return new AccountUpdateRequest(status, view.creditLimit(), view.cashCreditLimit(), view.currentBalance(),
                view.currentCycleCredit(), view.currentCycleDebit(), view.openDate(), view.expirationDate(),
                view.reissueDate(), view.groupId(), view.customer(), version);
    }
}
