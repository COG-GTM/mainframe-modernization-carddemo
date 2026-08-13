package com.carddemo.online.account;

import com.carddemo.model.dto.CardDemoCommarea;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COACTVWC — transaction CAVW (View Account).
 *
 * <p>The pseudo-conversational COMMAREA (COCOM01Y / {@link CardDemoCommarea}) is kept
 * in the HTTP session under {@link CardDemoCommarea#SESSION_KEY}.</p>
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountViewController {

    private final AccountViewService accountViewService;

    public AccountViewController(AccountViewService accountViewService) {
        this.accountViewService = accountViewService;
    }

    @PostMapping("/view")
    public ResponseEntity<AccountViewResponse> view(@RequestBody AccountViewRequest request, HttpSession session) {
        CardDemoCommarea commarea = OnlineSession.commarea(session);
        AccountViewResponse response = accountViewService.view(request, commarea);
        OnlineSession.store(session, commarea);
        return ResponseEntity.ok(response);
    }
}
