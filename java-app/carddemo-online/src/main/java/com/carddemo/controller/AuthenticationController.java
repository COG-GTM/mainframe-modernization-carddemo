package com.carddemo.controller;

import com.carddemo.dto.SessionContext;
import com.carddemo.enums.ProgramContext;
import com.carddemo.service.AuthenticationService;
import com.carddemo.service.AuthenticationService.AuthResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

        // Set Spring Security authentication context and persist to HTTP session
        String role = "ROLE_" + result.user().getUserType().name();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                result.user().getUserId(), null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

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
