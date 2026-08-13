package com.carddemo.online.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * COBOL program COCRDLIC — 9000-READ-FORWARD, 9100-READ-BACKWARDS, 9500-FILTER-RECORDS
 * and 1400-SETUP-MESSAGE, exercised against the real card file ordering.
 */
@DataJpaTest
class CardListServiceTest {

    private static final int CARDS = 20;

    @Autowired
    private CardBrowseRepository cardBrowseRepository;

    private CardListService service;
    private CardListState state;
    private CardDemoCommarea commarea;

    @BeforeEach
    void setUp() {
        service = new CardListService(cardBrowseRepository);
        state = new CardListState();
        commarea = new CardDemoCommarea();
        for (int i = 1; i <= CARDS; i++) {
            cardBrowseRepository.save(Card.builder()
                    .cardNumber(String.format("%016d", i))
                    .accountId(i % 2 == 0 ? 2L : 1L)
                    .cvvCode(100 + i)
                    .embossedName("CARD HOLDER " + i)
                    .expirationDate("2025-01-01")
                    .activeStatus("Y")
                    .build());
        }
    }

    private CardListResponse send(String action, String accountFilter, String cardFilter, List<String> selections) {
        return service.process(CardListRequest.builder()
                .action(action)
                .accountIdFilter(accountFilter)
                .cardNumberFilter(cardFilter)
                .selections(selections)
                .build(), state, commarea);
    }

    private CardListResponse enter() {
        return send(CardListRequest.ACTION_ENTER, null, null, null);
    }

    @Test
    void firstPageShowsSevenRowsAndAnnouncesMore() {
        CardListResponse response = enter();

        assertThat(response.getRows()).hasSize(CardWorkArea.MAX_SCREEN_LINES);
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000001");
        assertThat(response.getRows().get(6).getCardNumber()).isEqualTo("0000000000000007");
        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.isNextPageExists()).isTrue();
        assertThat(response.getInfoMessage()).isEqualTo(CardListService.INFORM_REC_ACTIONS);
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void pf8PagesForwardFromTheKeyAfterTheLastDisplayedRow() {
        enter();
        CardListResponse response = send(CardListRequest.ACTION_PF8, null, null, null);

        assertThat(response.getPageNumber()).isEqualTo(2);
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000008");
        assertThat(response.getRows().get(6).getCardNumber()).isEqualTo("0000000000000014");
    }

    @Test
    void pf7PagesBackwardsIntoDisplayOrder() {
        enter();
        send(CardListRequest.ACTION_PF8, null, null, null);
        CardListResponse response = send(CardListRequest.ACTION_PF7, null, null, null);

        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000001");
        assertThat(response.getRows().get(6).getCardNumber()).isEqualTo("0000000000000007");
    }

    @Test
    void pf7OnTheFirstPageRefusesToPageUp() {
        enter();
        CardListResponse response = send(CardListRequest.ACTION_PF7, null, null, null);

        assertThat(response.getErrorMessage()).isEqualTo(CardListService.NO_PREVIOUS_PAGES);
        assertThat(response.getPageNumber()).isEqualTo(1);
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000001");
    }

    @Test
    void shortLastPageReportsNoMoreRecordsThenNoMorePages() {
        enter();
        send(CardListRequest.ACTION_PF8, null, null, null);
        CardListResponse lastPage = send(CardListRequest.ACTION_PF8, null, null, null);

        assertThat(lastPage.getPageNumber()).isEqualTo(3);
        assertThat(lastPage.getRows()).hasSize(6);
        assertThat(lastPage.getRows().get(5).getCardNumber()).isEqualTo("0000000000000020");
        assertThat(lastPage.isNextPageExists()).isFalse();
        assertThat(lastPage.getErrorMessage()).isEqualTo(CardListService.NO_MORE_RECORDS);

        CardListResponse beyond = send(CardListRequest.ACTION_PF8, null, null, null);
        assertThat(beyond.getErrorMessage()).isEqualTo(CardListService.NO_MORE_PAGES);
        assertThat(beyond.getRows().get(0).getCardNumber()).isEqualTo("0000000000000015");
    }

    @Test
    void filtersOnAccountId() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, "00000000001", null, null);

        assertThat(response.getRows()).hasSize(CardWorkArea.MAX_SCREEN_LINES);
        assertThat(response.getRows()).allSatisfy(row -> assertThat(row.getAccountId()).isEqualTo("00000000001"));
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000001");
        assertThat(response.getRows().get(1).getCardNumber()).isEqualTo("0000000000000003");
    }

    @Test
    void filtersOnCardNumber() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, "0000000000000009", null);

        assertThat(response.getRows()).hasSize(1);
        assertThat(response.getRows().get(0).getCardNumber()).isEqualTo("0000000000000009");
        assertThat(response.getErrorMessage()).isEqualTo(CardListService.NO_MORE_RECORDS);
    }

    @Test
    void reportsWhenNothingMatchesTheFilter() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, "00000000099", null, null);

        assertThat(response.getRows()).isEmpty();
        assertThat(response.getErrorMessage()).isEqualTo(CardListService.NO_RECORDS_FOUND);
        assertThat(response.getInfoMessage()).isNull();
    }

    @Test
    void rejectsAccountFilterThatIsNotElevenDigits() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, "123", null, null);

        assertThat(response.getErrorMessage()).isEqualTo(CardListService.ACCOUNT_FILTER_INVALID);
        assertThat(response.getRows()).isEmpty();
        assertThat(response.isSelectRowsProtected()).isTrue();
    }

    @Test
    void rejectsCardFilterThatIsNotSixteenDigits() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, "12345", null);

        assertThat(response.getErrorMessage()).isEqualTo(CardListService.CARD_FILTER_INVALID);
        assertThat(response.getRows()).isEmpty();
    }

    @Test
    void treatsAsteriskAndZeroesAsNoFilter() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, "*", "0000000000000000", null);

        assertThat(response.getRows()).hasSize(CardWorkArea.MAX_SCREEN_LINES);
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void refusesMoreThanOneSelectedRow() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, null,
                Arrays.asList("S", "", "U", "", "", "", ""));

        assertThat(response.getErrorMessage()).isEqualTo(CardListService.MORE_THAN_1_ACTION);
        assertThat(response.getRows().get(0).isSelectionError()).isTrue();
        assertThat(response.getRows().get(2).isSelectionError()).isTrue();
    }

    @Test
    void rejectsAnActionCodeThatIsNotSorU() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, null,
                Arrays.asList("", "X", "", "", "", "", ""));

        assertThat(response.getErrorMessage()).isEqualTo(CardListService.INVALID_ACTION_CODE);
        assertThat(response.getRows().get(1).isSelectionError()).isTrue();
    }

    @Test
    void selectingSTransfersToTheCardDetailProgram() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, null,
                Arrays.asList("", "", "S", "", "", "", ""));

        assertThat(response.getNextProgram()).isEqualTo(CardWorkArea.DETAIL_PROGRAM);
        assertThat(response.getNextTransaction()).isEqualTo(CardWorkArea.DETAIL_TRANSACTION);
        assertThat(response.getSelectedCardNumber()).isEqualTo("0000000000000003");
        assertThat(commarea.getCardNumber()).isEqualTo("0000000000000003");
        assertThat(commarea.getFromProgram()).isEqualTo(CardWorkArea.LIST_PROGRAM);
    }

    @Test
    void selectingUTransfersToTheCardUpdateProgram() {
        CardListResponse response = send(CardListRequest.ACTION_ENTER, null, null,
                Arrays.asList("U", "", "", "", "", "", ""));

        assertThat(response.getNextProgram()).isEqualTo(CardWorkArea.UPDATE_PROGRAM);
        assertThat(response.getSelectedCardNumber()).isEqualTo("0000000000000001");
    }

    @Test
    void pf3ReturnsToTheMainMenuAndClearsThePageKeys() {
        enter();
        CardListResponse response = send(CardListRequest.ACTION_PF3, null, null, null);

        assertThat(response.getNextProgram()).isEqualTo(CardWorkArea.MENU_PROGRAM);
        assertThat(response.getErrorMessage()).isEqualTo(CardListService.EXIT_MESSAGE);
        assertThat(state.getScreenNumber()).isEqualTo(1);
        assertThat(state.getFirstCardNumber()).isEmpty();
    }
}
