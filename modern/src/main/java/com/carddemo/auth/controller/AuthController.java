package com.carddemo.auth.controller;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.UserInfoResponse;
import com.carddemo.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 *
 * Migrated from: COSGN00C.cbl (Signon Screen for the CardDemo Application)
 * Original CICS Transaction: CC00
 * Original BMS Map: COSGN00 / COSGN0A
 *
 * Endpoint mapping from COBOL:
 *   POST /api/v1/auth/login  <- PROCESS-ENTER-KEY + READ-USER-SEC-FILE
 *   POST /api/v1/auth/logout <- PF3 key handler (CCDA-MSG-THANK-YOU)
 *   GET  /api/v1/auth/me     <- COMMAREA read (CDEMO-USER-ID, CDEMO-USER-TYPE)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticate user and return JWT token.
     *
     * Migrated from: COSGN00C.cbl, PROCESS-ENTER-KEY (lines 108-140)
     *                and READ-USER-SEC-FILE (lines 209-257)
     * Original flow: receive BMS map -> validate fields -> READ USRSEC ->
     *                compare password -> XCTL to COADM01C or COMEN01C
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(
                request.userId(), request.password());
        return ResponseEntity.ok(response);
    }

    /**
     * Invalidate session / logout.
     *
     * Migrated from: COSGN00C.cbl, PF3 key handler (lines 88-90)
     * Original COBOL: MOVE CCDA-MSG-THANK-YOU TO WS-MESSAGE
     *                 PERFORM SEND-PLAIN-TEXT
     *                 EXEC CICS RETURN END-EXEC
     *
     * Note: With stateless JWT, logout is handled client-side by discarding
     * the token. This endpoint provides a standard logout contract.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Thank you for using CardDemo!"));
    }

    /**
     * Return current authenticated user info from JWT token.
     *
     * Migrated from: COSGN00C.cbl COMMAREA fields
     * Original COMMAREA: CDEMO-USER-ID, CDEMO-USER-TYPE,
     *                    CDEMO-FROM-TRANID, CDEMO-FROM-PROGRAM
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserInfoResponse response = authService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }
}
