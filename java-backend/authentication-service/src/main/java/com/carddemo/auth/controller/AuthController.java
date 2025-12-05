package com.carddemo.auth.controller;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.TokenValidationResponse;
import com.carddemo.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller
 * 
 * REST API endpoints replacing the COSGN00C COBOL program (Transaction CC00).
 * 
 * Original mainframe flow:
 * - User enters credentials on COSGN00 BMS screen
 * - COSGN00C validates against USRSEC VSAM file
 * - On success, routes to admin menu (COADM01C) or user menu (COMEN01C)
 * 
 * Modernized flow:
 * - Client sends credentials to POST /api/auth/login
 * - Service validates and returns JWT token
 * - Client uses token for subsequent API calls
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints (replaces COSGN00C)")
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * User login endpoint
     * 
     * Replaces the PROCESS-ENTER-KEY and READ-USER-SEC-FILE paragraphs from COSGN00C
     * 
     * @param request login credentials (userId, password)
     * @return JWT token and user information
     */
    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user and return JWT token. Replaces COSGN00C transaction CC00."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - missing or invalid input"
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * User logout endpoint
     * 
     * Replaces the PF3 key handling in COSGN00C:
     *   WHEN DFHPF3
     *       MOVE CCDA-MSG-THANK-YOU TO WS-MESSAGE
     *       PERFORM SEND-PLAIN-TEXT
     * 
     * @param authHeader Authorization header containing JWT token
     * @return success message
     */
    @PostMapping("/logout")
    @Operation(
            summary = "User Logout",
            description = "Invalidate user session. Replaces PF3 key handling in COSGN00C."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    })
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
        }
        return ResponseEntity.ok("Thank you for using CardDemo Application...");
    }

    /**
     * Token validation endpoint
     * 
     * Used by API Gateway and other services to validate JWT tokens
     * and retrieve user information for authorization decisions.
     * 
     * @param authHeader Authorization header containing JWT token
     * @return token validation result with user info
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validate Token",
            description = "Validate JWT token and return user information for authorization."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(schema = @Schema(implementation = TokenValidationResponse.class))
            )
    })
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Missing or invalid Authorization header")
                    .build());
        }

        String token = authHeader.substring(7);
        TokenValidationResponse response = authenticationService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
