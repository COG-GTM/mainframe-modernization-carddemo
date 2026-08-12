package com.carddemo.online.auth;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.common.CommareaSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COSGN00C (transaction CC00), BMS mapset COSGN00.
 *
 * <p>Each AID key of {@code EVALUATE EIBAID} is exposed as its own endpoint.
 */
@RestController
@RequestMapping("/api/signon")
public class SignOnController {

    private final SignOnService signOnService;

    public SignOnController(SignOnService signOnService) {
        this.signOnService = signOnService;
    }

    /** First entry into the transaction, {@code EIBCALEN = 0}. */
    @GetMapping
    public SignOnResponse signOnScreen(HttpSession session) {
        CardDemoCommarea commarea = new CardDemoCommarea();
        CommareaSession.store(session, commarea);
        return signOnService.initialScreen(commarea);
    }

    /** DFHENTER. */
    @PostMapping("/enter")
    public SignOnResponse enter(@RequestBody SignOnRequest request, HttpSession session) {
        CardDemoCommarea commarea = CommareaSession.require(session);
        SignOnResponse response = signOnService.processEnterKey(request, commarea);
        CommareaSession.store(session, commarea);
        return response;
    }

    /** DFHPF3 — exit with CCDA-MSG-THANK-YOU. */
    @PostMapping("/pf3")
    public SignOnResponse pf3(HttpSession session) {
        session.removeAttribute(CardDemoCommarea.SESSION_KEY);
        return signOnService.processPf3Key();
    }

    /** {@code WHEN OTHER} — any other AID key. */
    @PostMapping("/other-key")
    public SignOnResponse otherKey() {
        return signOnService.processOtherKey();
    }
}
