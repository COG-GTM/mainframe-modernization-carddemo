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
 * COBOL program: COCRDUPC — transaction CCUP (Credit Card Update).
 *
 * <p>The COMMAREA (COCOM01Y) and WS-THIS-PROGCOMMAREA ({@link CardUpdateState}) both
 * live in the HTTP session, which is what makes the confirm-then-commit flow
 * pseudo-conversational.</p>
 */
@RestController
@RequestMapping("/api/cards")
public class CardUpdateController {

    private final CardUpdateService cardUpdateService;

    public CardUpdateController(CardUpdateService cardUpdateService) {
        this.cardUpdateService = cardUpdateService;
    }

    @PostMapping("/update")
    public ResponseEntity<CardUpdateResponse> update(@RequestBody CardUpdateRequest request, HttpSession session) {
        CardDemoCommarea commarea = OnlineSession.commarea(session);
        CardUpdateState state = state(session);
        CardUpdateResponse response = cardUpdateService.process(request, state, commarea);
        session.setAttribute(CardUpdateState.SESSION_KEY, state);
        OnlineSession.store(session, commarea);
        return ResponseEntity.ok(response);
    }

    private CardUpdateState state(HttpSession session) {
        Object stored = session.getAttribute(CardUpdateState.SESSION_KEY);
        if (stored instanceof CardUpdateState) {
            return (CardUpdateState) stored;
        }
        return new CardUpdateState();
    }
}
