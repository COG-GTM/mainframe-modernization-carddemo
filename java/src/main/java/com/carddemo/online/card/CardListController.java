package com.carddemo.online.card;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.account.OnlineSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COCRDLIC — transaction CCLI (List Credit Cards).
 *
 * <p>Both the COMMAREA (COCOM01Y) and WS-THIS-PROGCOMMAREA ({@link CardListState},
 * which carries the paging keys) are kept in the HTTP session.</p>
 */
@RestController
@RequestMapping("/api/cards")
public class CardListController {

    private final CardListService cardListService;

    public CardListController(CardListService cardListService) {
        this.cardListService = cardListService;
    }

    @PostMapping("/list")
    public ResponseEntity<CardListResponse> list(@RequestBody CardListRequest request, HttpSession session) {
        CardDemoCommarea commarea = OnlineSession.commarea(session);
        CardListState state = state(session);
        CardListResponse response = cardListService.process(request, state, commarea);
        session.setAttribute(CardListState.SESSION_KEY, state);
        OnlineSession.store(session, commarea);
        return ResponseEntity.ok(response);
    }

    private CardListState state(HttpSession session) {
        Object stored = session.getAttribute(CardListState.SESSION_KEY);
        if (stored instanceof CardListState) {
            return (CardListState) stored;
        }
        return new CardListState();
    }
}
