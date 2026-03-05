package com.carddemo.controller;

import com.carddemo.dto.SessionContext;
import com.carddemo.enums.ProgramContext;
import com.carddemo.service.AuthenticationService;
import com.carddemo.service.AuthenticationService.AuthResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller — replaces COSGN00C CICS transaction.
 * POST /api/auth/login  — validate credentials, populate session
 * POST /api/auth/logout — invalidate session
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final SessionContext sessionContext;

    public AuthenticationController(AuthenticationService authenticationService,
                                    SessionContext sessionContext) {
        this.authenticationService = authenticationService;
        this.sessionContext = sessionContext;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        AuthResult result = authenticationService.authenticate(request.userId(), request.password());

        if (!result.success()) {
            return ResponseEntity.status(401).body(Map.of("error", result.errorMessage()));
        }

        // Populate COMMAREA-equivalent session context
        sessionContext.setUserId(result.user().getUserId());
        sessionContext.setUserType(result.user().getUserType().name());
        sessionContext.setPgmContext(ProgramContext.ENTER);
        sessionContext.setFromProgram("COSGN00C");

        return ResponseEntity.ok(Map.of(
            "userId", result.user().getUserId(),
            "userType", result.user().getUserType().name(),
            "firstName", result.user().getFirstName(),
            "lastName", result.user().getLastName()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    public record LoginRequest(String userId, String password) {}
}
