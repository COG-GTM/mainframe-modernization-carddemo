package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authentication service migrated from COBOL program COSGN00C.cbl.
 *
 * Implements the signon logic:
 *   1. Convert userId to uppercase (FUNCTION UPPER-CASE)
 *   2. Look up user by ID in USRSEC file (now JPA repository)
 *   3. If not found → "User not found" (RESP-CD 13 in COBOL)
 *   4. If password mismatch → "Wrong Password"
 *   5. If match → generate JWT with user info (replaces COMMAREA + XCTL)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // COBOL: MOVE FUNCTION UPPER-CASE(USERIDI OF COSGN0AI) TO WS-USER-ID
        String userId = request.getUserId().toUpperCase().trim();
        String password = request.getPassword();

        // COBOL: EXEC CICS READ DATASET(WS-USRSEC-FILE) INTO(SEC-USER-DATA) ...
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        // COBOL: WHEN 13 → 'User not found. Try again ...'
                        new AuthenticationException("User not found. Try again ..."));

        // COBOL: IF SEC-USR-PWD = WS-USER-PWD
        if (!user.getPassword().equals(password)) {
            // COBOL: 'Wrong Password. Try again ...'
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        // COBOL: On success, populate COMMAREA and XCTL to menu program
        // Now we generate a JWT token instead
        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getUserType(),
                user.getFirstName(),
                user.getLastName()
        );

        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();
    }

    public LoginResponse validateToken(String token) {
        Claims claims = jwtUtil.validateToken(token);

        return LoginResponse.builder()
                .token(token)
                .userId(claims.getSubject())
                .firstName(claims.get("firstName", String.class))
                .lastName(claims.get("lastName", String.class))
                .userType(claims.get("userType", String.class))
                .build();
    }
}
