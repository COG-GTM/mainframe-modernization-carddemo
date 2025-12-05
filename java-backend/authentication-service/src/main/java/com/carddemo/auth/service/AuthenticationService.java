package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.TokenValidationResponse;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.common.exception.AuthenticationException;
import com.carddemo.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication service implementing login/logout/validate operations.
 * 
 * Replaces COBOL program COSGN00C.cbl functionality:
 * 
 * READ-USER-SEC-FILE:
 *   EXEC CICS READ
 *        DATASET   (WS-USRSEC-FILE)
 *        INTO      (SEC-USER-DATA)
 *        RIDFLD    (WS-USER-ID)
 *   END-EXEC
 * 
 * Password validation:
 *   IF SEC-USR-PWD = WS-USER-PWD
 *       [authentication successful]
 *   ELSE
 *       MOVE 'Wrong Password. Try again ...' TO WS-MESSAGE
 * 
 * User type routing:
 *   IF CDEMO-USRTYP-ADMIN
 *       EXEC CICS XCTL PROGRAM ('COADM01C') ...
 *   ELSE
 *       EXEC CICS XCTL PROGRAM ('COMEN01C') ...
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUserId());

        User user = userRepository.findByUserIdAndIsActiveTrue(request.getUserId().toUpperCase())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUserId());
                    return new AuthenticationException("User not found. Try again ...");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", request.getUserId());
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getUserId(),
                user.getUserType(),
                user.getFirstName(),
                user.getLastName()
        );

        log.info("Login successful for user: {}, type: {}", user.getUserId(), user.getUserType());

        return LoginResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .build();
    }

    public void logout(String token) {
        log.info("Logout requested");
        // TODO: Implement token blacklisting for proper logout
        // For now, client-side token removal is sufficient
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .build();
            }

            String userId = jwtService.extractUserId(token);
            String userType = jwtService.extractUserType(token);
            long expiresAt = jwtService.getExpirationDate(token).getTime();

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .userType(userType)
                    .expiresAt(expiresAt)
                    .build();
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }
    }
}
