package com.carddemo.session.web;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CommareaSessionStore;
import com.carddemo.session.CurrentUserProvider;
import com.carddemo.session.NavigationService;
import com.carddemo.session.PfKey;
import com.carddemo.session.ProgramRegistry;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRegistry;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.session.UserType;

import jakarta.servlet.http.HttpSession;

/**
 * Central pseudo-conversational navigation endpoint. It keeps the {@link CardDemoCommarea}
 * in the HTTP session and reproduces the CICS sign-on → menu → function → back flow over
 * REST. WAVE 3 function waves register a {@link ScreenHandler} per screen and this
 * controller dispatches to them; with no handler registered a turn is pure navigation
 * (transfer-of-control + ENTER/RE-ENTER bookkeeping).
 */
@RestController
@RequestMapping("/api/nav")
public class NavigationController {

    private final NavigationService navigation;
    private final CommareaSessionStore store;
    private final CurrentUserProvider currentUser;
    private final ProgramRegistry registry;
    private final ScreenRegistry screens;

    public NavigationController(NavigationService navigation, CommareaSessionStore store,
            CurrentUserProvider currentUser, ProgramRegistry registry, ScreenRegistry screens) {
        this.navigation = navigation;
        this.store = store;
        this.currentUser = currentUser;
        this.registry = registry;
        this.screens = screens;
    }

    /**
     * Establish the conversation for the already-authenticated user and route to the admin
     * or main menu based on role — mirrors {@code COSGN00C} post-authentication routing.
     */
    @PostMapping("/signon")
    public NavigationResponse signon(HttpSession session) {
        CardDemoCommarea commarea = store.getOrCreate(session);
        String userId = currentUser.userId().orElse(null);
        UserType userType = currentUser.userType().orElse(UserType.USER);
        navigation.signon(commarea, userId, userType);
        store.save(session, commarea);
        return NavigationResponse.from(commarea, registry, null, null);
    }

    /** Return the current commarea routing state without changing it. */
    @GetMapping
    public NavigationResponse state(HttpSession session) {
        CardDemoCommarea commarea = store.getOrCreate(session);
        return NavigationResponse.from(commarea, registry, null, null);
    }

    /**
     * Perform one navigation turn: apply the PF key, launch a selected transaction, and/or
     * dispatch to the current screen's handler.
     */
    @PostMapping
    public ResponseEntity<NavigationResponse> navigate(@RequestBody(required = false) NavigationRequest request,
            HttpSession session) {
        CardDemoCommarea commarea = store.getOrCreate(session);
        NavigationRequest req = request == null
            ? new NavigationRequest(null, null, null)
            : request;
        PfKey pfKey = PfKey.from(req.pfKey());
        ScreenRequest screenRequest = new ScreenRequest(pfKey, req.fields());

        String message = null;
        Object model = null;

        if (pfKey.action() == com.carddemo.session.CommonAction.BACK) {
            navigation.back(commarea);
        } else if (req.tranId() != null && !req.tranId().isBlank()) {
            Optional<CardDemoProgram> target = registry.byTranId(req.tranId());
            if (target.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(NavigationResponse.from(commarea, registry,
                        "Unknown transaction: " + req.tranId(), null));
            }
            CardDemoProgram program = target.get();
            UserType userType = commarea.getUserType();
            if (!program.isAccessibleBy(userType)) {
                message = "No access - Admin Only option...";
            } else {
                CardDemoProgram from = navigation.currentProgram(commarea).orElse(null);
                navigation.transferControl(commarea, from, program);
                ScreenResult result = dispatch(program, screenRequest, commarea);
                if (result != null) {
                    message = result.message();
                    model = result.model();
                }
            }
        } else {
            CardDemoProgram current = navigation.currentProgram(commarea).orElse(null);
            ScreenResult result = dispatch(current, screenRequest, commarea);
            if (result != null) {
                message = result.message();
                model = result.model();
            } else {
                navigation.beginTurn(commarea);
            }
        }

        store.save(session, commarea);
        return ResponseEntity.ok(NavigationResponse.from(commarea, registry, message, model));
    }

    /**
     * Dispatch to the screen handler for {@code program} (if any) and apply the resulting
     * transfer-of-control. Returns {@code null} when no handler is registered.
     */
    private ScreenResult dispatch(CardDemoProgram program, ScreenRequest request,
            CardDemoCommarea commarea) {
        if (program == null) {
            return null;
        }
        Optional<ScreenHandler> handler = screens.forProgram(program);
        if (handler.isEmpty()) {
            return null;
        }
        ScreenResult result = handler.get().handle(request, commarea);
        applyResult(commarea, result);
        return result;
    }

    private void applyResult(CardDemoCommarea commarea, ScreenResult result) {
        switch (result.type()) {
            case STAY -> navigation.stay(commarea);
            case BACK -> navigation.back(commarea);
            case TRANSFER -> {
                CardDemoProgram from = navigation.currentProgram(commarea).orElse(null);
                navigation.transferControl(commarea, from, result.target());
            }
        }
    }
}
