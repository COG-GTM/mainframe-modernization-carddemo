package uk.co.nationwide.cards.posting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.nationwide.cards.posting.api.PostTransactionRequest;
import uk.co.nationwide.cards.posting.api.PostTransactionResponse;
import uk.co.nationwide.cards.posting.domain.Account;
import uk.co.nationwide.cards.posting.domain.Card;
import uk.co.nationwide.cards.posting.domain.CardXref;
import uk.co.nationwide.cards.posting.domain.TranCategoryBalance;
import uk.co.nationwide.cards.posting.domain.Transaction;
import uk.co.nationwide.cards.posting.repository.AccountRepository;
import uk.co.nationwide.cards.posting.repository.CardRepository;
import uk.co.nationwide.cards.posting.repository.CardXrefRepository;
import uk.co.nationwide.cards.posting.repository.TranCategoryBalanceRepository;
import uk.co.nationwide.cards.posting.repository.TransactionRepository;

/**
 * Equivalence tests against the COBOL CBTRN02C paragraphs.
 *
 * <p>Each test maps to one of the rejection paths in the legacy program
 * (paragraph <code>1500-VALIDATE-TRAN</code>) plus the happy path
 * (<code>2000-POST-TRANSACTION</code>).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionPostingServiceTest {

    private static final String CARD_NUM = "4929123456789010";
    private static final Long ACCT_ID = 12345678901L;
    private static final Instant FIXED_NOW = Instant.parse("2025-06-15T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

    @Mock private CardXrefRepository xrefRepo;
    @Mock private CardRepository cardRepo;
    @Mock private AccountRepository accountRepo;
    @Mock private TransactionRepository transactionRepo;
    @Mock private TranCategoryBalanceRepository categoryBalanceRepo;

    private TransactionPostingService service;

    @BeforeEach
    void setUp() {
        service = new TransactionPostingService(
                xrefRepo, cardRepo, accountRepo,
                transactionRepo, categoryBalanceRepo, FIXED_CLOCK);
    }

    @Test
    @DisplayName("happy path: valid request posts transaction and updates account balance")
    void postsValidTransaction() {
        givenValidEstate();

        PostTransactionRequest request = buildRequest(new BigDecimal("25.00"));
        PostTransactionResponse response = service.postTransaction(request);

        assertThat(response.getTranId()).isEqualTo(request.getTranId());
        assertThat(response.getAcctId()).isEqualTo(ACCT_ID);
        assertThat(response.getAcctNewBalance()).isEqualByComparingTo("125.00");
        assertThat(response.getProcessedAt()).isEqualTo(FIXED_NOW);
        verify(transactionRepo).save(any(Transaction.class));
        verify(accountRepo).save(any(Account.class));
        verify(categoryBalanceRepo).save(any(TranCategoryBalance.class));
    }

    @Test
    @DisplayName("rejects (code 100) when card number is not in the cross-reference")
    void rejectsWhenCardNotInXref() {
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.CARD_NOT_FOUND_IN_XREF);

        verify(accountRepo, never()).save(any());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    @DisplayName("rejects (code 101) when xref points at a missing account")
    void rejectsWhenAccountMissing() {
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("rejects (code 102) when account is inactive")
    void rejectsWhenAccountInactive() {
        Account inactive = activeAccount();
        inactive.setAcctActiveStatus("N");
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.ACCOUNT_INACTIVE);
    }

    @Test
    @DisplayName("rejects (code 103) when card is inactive")
    void rejectsWhenCardInactive() {
        Card inactive = activeCard();
        inactive.setCardActiveStatus("N");
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.of(activeAccount()));
        when(cardRepo.findById(CARD_NUM)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.CARD_INACTIVE);
    }

    @Test
    @DisplayName("rejects (code 104) when card has expired (expiration date is today or earlier)")
    void rejectsWhenCardExpired() {
        Card expired = activeCard();
        expired.setCardExpirationDate(LocalDate.ofInstant(FIXED_NOW, ZoneOffset.UTC));
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.of(activeAccount()));
        when(cardRepo.findById(CARD_NUM)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.CARD_EXPIRED);
    }

    @Test
    @DisplayName("rejects (code 105) when transaction would exceed credit limit")
    void rejectsWhenExceedsCreditLimit() {
        Account nearLimit = activeAccount();
        nearLimit.setAcctCurrBal(new BigDecimal("4990.00"));
        nearLimit.setAcctCreditLimit(new BigDecimal("5000.00"));
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.of(nearLimit));
        when(cardRepo.findById(CARD_NUM)).thenReturn(Optional.of(activeCard()));

        assertThatThrownBy(() -> service.postTransaction(buildRequest(new BigDecimal("25.00"))))
                .isInstanceOf(TransactionRejectedException.class)
                .extracting(e -> ((TransactionRejectedException) e).failure())
                .isEqualTo(ValidationFailure.EXCEEDS_CREDIT_LIMIT);
    }

    @Test
    @DisplayName("increments existing category balance rather than overwriting")
    void incrementsCategoryBalance() {
        givenValidEstate();
        TranCategoryBalance existing = new TranCategoryBalance(ACCT_ID, "01", 5411, new BigDecimal("40.00"));
        when(categoryBalanceRepo.findById(any(TranCategoryBalance.Key.class)))
                .thenReturn(Optional.of(existing));

        service.postTransaction(buildRequest(new BigDecimal("25.00")));

        assertThat(existing.getTranCatBal()).isEqualByComparingTo("65.00");
        verify(categoryBalanceRepo, times(1)).save(existing);
    }

    private void givenValidEstate() {
        when(xrefRepo.findById(CARD_NUM)).thenReturn(Optional.of(xref()));
        when(accountRepo.findById(ACCT_ID)).thenReturn(Optional.of(activeAccount()));
        when(cardRepo.findById(CARD_NUM)).thenReturn(Optional.of(activeCard()));
        when(categoryBalanceRepo.findById(any(TranCategoryBalance.Key.class)))
                .thenReturn(Optional.empty());
    }

    private CardXref xref() {
        CardXref x = new CardXref();
        x.setXrefCardNum(CARD_NUM);
        x.setXrefAcctId(ACCT_ID);
        x.setXrefCustId(100000001L);
        return x;
    }

    private Account activeAccount() {
        Account a = new Account();
        a.setAcctId(ACCT_ID);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal("100.00"));
        a.setAcctCreditLimit(new BigDecimal("5000.00"));
        a.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        return a;
    }

    private Card activeCard() {
        Card c = new Card();
        c.setCardNum(CARD_NUM);
        c.setCardAcctId(ACCT_ID);
        c.setCardActiveStatus("Y");
        c.setCardExpirationDate(LocalDate.of(2030, 4, 30));
        return c;
    }

    private PostTransactionRequest buildRequest(BigDecimal amount) {
        PostTransactionRequest r = new PostTransactionRequest();
        r.setTranId("TXN0000000000001");
        r.setTranTypeCd("01");
        r.setTranCatCd(5411);
        r.setTranSource("POS");
        r.setTranDesc("Demo merchant purchase");
        r.setTranAmt(amount);
        r.setTranMerchantId(900000001L);
        r.setTranMerchantName("WAITROSE SWINDON");
        r.setTranMerchantCity("Swindon");
        r.setTranMerchantZip("SN1 1AA");
        r.setTranCardNum(CARD_NUM);
        r.setTranOrigTs(FIXED_NOW.minusSeconds(60));
        return r;
    }
}
