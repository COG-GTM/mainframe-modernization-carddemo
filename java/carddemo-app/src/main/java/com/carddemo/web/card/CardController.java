package com.carddemo.web.card;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.card.CardNotFoundException;
import com.carddemo.service.card.CardService;
import com.carddemo.service.card.CardValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CommareaSessionStore;
import com.carddemo.session.CurrentUserProvider;
import com.carddemo.session.UserType;
import com.carddemo.web.card.dto.CardDetailResponse;
import com.carddemo.web.card.dto.CardListResponse;
import com.carddemo.web.card.dto.CardUpdateRequest;
import com.carddemo.web.card.dto.CardUpdateResponse;

import jakarta.servlet.http.HttpSession;

/**
 * REST surface for the WAVE 3 card functions — the ported {@code COCRDLIC} (list),
 * {@code COCRDSLC} (detail) and {@code COCRDUPC} (update) online programs.
 *
 * <ul>
 *   <li>{@code GET  /api/cards} — paged, filterable card list.</li>
 *   <li>{@code GET  /api/cards/{cardNumber}} — card detail.</li>
 *   <li>{@code PUT  /api/cards/{cardNumber}} — card update.</li>
 * </ul>
 *
 * <p>Access mirrors COCRDLIC: an admin ({@code ROLE_ADMIN}) sees every card and may filter
 * freely, while a regular user is restricted to the account carried in the session COMMAREA
 * ("Only the ones associated with ACCT in COMMAREA if user is not admin"). Field-edit and
 * not-found failures are translated to the verbatim COBOL screen messages via the
 * controller-local exception handlers (no global handler is introduced).</p>
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final CurrentUserProvider currentUser;
    private final CommareaSessionStore commareaStore;

    public CardController(CardService cardService, CurrentUserProvider currentUser,
            CommareaSessionStore commareaStore) {
        this.cardService = cardService;
        this.currentUser = currentUser;
        this.commareaStore = commareaStore;
    }

    @GetMapping
    public CardListResponse list(
            @RequestParam(name = "accountId", required = false) String accountId,
            @RequestParam(name = "cardNumber", required = false) String cardNumber,
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpSession session) {
        String accountFilter = resolveAccountFilter(accountId, session);
        return cardService.listCards(accountFilter, cardNumber, page);
    }

    @GetMapping("/{cardNumber}")
    public CardDetailResponse detail(
            @PathVariable("cardNumber") String cardNumber,
            @RequestParam(name = "accountId", required = false) String accountId) {
        return cardService.getCard(cardNumber, accountId);
    }

    @PutMapping("/{cardNumber}")
    public CardUpdateResponse update(
            @PathVariable("cardNumber") String cardNumber,
            @RequestBody(required = false) CardUpdateRequest request) {
        return cardService.updateCard(cardNumber, request);
    }

    /**
     * Enforce the COCRDLIC admin rule: admins filter by whatever account they pass (or none),
     * regular users are pinned to the account currently selected in their COMMAREA, falling
     * back to the account they supplied when the conversation has no selected account yet.
     */
    private String resolveAccountFilter(String requestedAccountId, HttpSession session) {
        boolean admin = currentUser.userType().orElse(UserType.USER) == UserType.ADMIN;
        if (admin) {
            return requestedAccountId;
        }
        CardDemoCommarea commarea = commareaStore.get(session);
        String contextAccount = commarea == null ? null : commarea.getAccountInfo().getAcctId();
        return contextAccount != null ? contextAccount : requestedAccountId;
    }

    @ExceptionHandler(CardValidationException.class)
    ResponseEntity<CardErrorResponse> handleValidation(CardValidationException ex) {
        return ResponseEntity.badRequest().body(new CardErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CardNotFoundException.class)
    ResponseEntity<CardErrorResponse> handleNotFound(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CardErrorResponse(ex.getMessage()));
    }

    /** Error body carrying the verbatim COBOL screen message. */
    public record CardErrorResponse(String message) {
    }
}
