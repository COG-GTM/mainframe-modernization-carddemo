package com.carddemo.auth.controller;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.TokenValidationRequest;
import com.carddemo.auth.dto.TokenValidationResponse;
import com.carddemo.auth.service.AuthenticationService;
import com.carddemo.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * 
 * Replaces COBOL program COSGN00C.cbl (transaction CC00):
 * - POST /api/auth/login   -> PROCESS-ENTER-KEY + READ-USER-SEC-FILE
 * - POST /api/auth/logout  -> DFHPF3 handler (Thank you message)
 * - POST /api/auth/validate -> Token validation (replaces COMMAREA validation)
 * 
 * Original COBOL transaction flow:
 *   CC00 -> COSGN00C -> validates credentials -> routes to COADM01C or COMEN01C
 * 
 * New REST API flow:
 *   POST /api/auth/login -> returns JWT token with user info
 *   Client uses token for subsequent requests
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API - replaces COSGN00C (CC00)")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT token. " +
                    "Replaces COSGN00C READ-USER-SEC-FILE and password validation."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUserId());
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logs out the current user. " +
                    "Replaces COSGN00C DFHPF3 handler that displays 'Thank you' message."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Thank you for using CardDemo"));
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Validate token",
            description = "Validates JWT token and returns user information. " +
                    "Replaces COMMAREA validation between CICS programs."
    )
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
            @Valid @RequestBody TokenValidationRequest request) {
        TokenValidationResponse response = authenticationService.validateToken(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
