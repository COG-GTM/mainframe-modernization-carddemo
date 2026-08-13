package com.carddemo.online.account;

import com.carddemo.model.dto.CardDemoCommarea;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COACTUPC — transaction CAUP (Update Account).
 *
 * <p>Each POST is one pseudo-conversational turn: the COMMAREA (COCOM01Y) and the
 * program area WS-THIS-PROGCOMMAREA ({@link AccountUpdateState}) are kept in the
 * HTTP session, as CICS keeps them on the RETURN.</p>
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountUpdateController {

    private final AccountUpdateService accountUpdateService;

    public AccountUpdateController(AccountUpdateService accountUpdateService) {
        this.accountUpdateService = accountUpdateService;
    }

    @PostMapping("/update")
    public ResponseEntity<AccountUpdateResponse> update(@RequestBody AccountUpdateRequest request,
            HttpSession session) {
        CardDemoCommarea commarea = OnlineSession.commarea(session);
        AccountUpdateState state = state(session);
        AccountUpdateResponse response = accountUpdateService.process(request, state, commarea);
        session.setAttribute(AccountUpdateState.SESSION_KEY, state);
        OnlineSession.store(session, commarea);
        return ResponseEntity.ok(response);
    }

    private AccountUpdateState state(HttpSession session) {
        Object stored = session.getAttribute(AccountUpdateState.SESSION_KEY);
        if (stored instanceof AccountUpdateState) {
            return (AccountUpdateState) stored;
        }
        return new AccountUpdateState();
    }
}
