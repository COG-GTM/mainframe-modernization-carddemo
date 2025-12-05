package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.TokenValidationResponse;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 * 
 * Implements the authentication logic from COSGN00C COBOL program.
 * 
 * Original COBOL flow (READ-USER-SEC-FILE paragraph):
 * 1. Read user record from USRSEC file using user ID as key
 * 2. Compare password: IF SEC-USR-PWD = WS-USER-PWD
 * 3. On success: Set CDEMO-USER-ID, CDEMO-USER-TYPE in COMMAREA
 * 4. Route to appropriate menu based on user type:
 *    - CDEMO-USRTYP-ADMIN -> XCTL to COADM01C
 *    - Otherwise -> XCTL to COMEN01C
 * 5. On failure: Display error message
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and generate JWT token
     * 
     * Replaces the READ-USER-SEC-FILE and password validation logic from COSGN00C
     * 
     * @param request login credentials
     * @return login response with JWT token
     * @throws AuthenticationException if authentication fails
     */
    public LoginResponse login(LoginRequest request) {
        // Convert to uppercase as mainframe does: FUNCTION UPPER-CASE(USERIDI OF COSGN0AI)
        String userId = request.getUserId().toUpperCase();

        // Find user - equivalent to CICS READ DATASET
        User user = userRepository.findByUserIdIgnoreCase(userId)
                .orElseThrow(() -> new AuthenticationException("User not found. Try again ..."));

        // Validate password - equivalent to: IF SEC-USR-PWD = WS-USER-PWD
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        // Generate JWT token - replaces setting COMMAREA values
        String token = tokenProvider.generateToken(user);

        log.info("User {} authenticated successfully. Type: {}", userId, user.getUserType());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInSeconds())
                .userId(user.getUserId())
                .userType(user.getUserType())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Validate JWT token and return user information
     * 
     * Used by API Gateway and other services to validate tokens
     * 
     * @param token JWT token to validate
     * @return validation response with user info
     */
    public TokenValidationResponse validateToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Invalid or expired token")
                    .build();
        }

        String userId = tokenProvider.getUserIdFromToken(token);
        String userType = tokenProvider.getUserTypeFromToken(token);

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .userType(User.UserType.valueOf(userType))
                .build();
    }

    /**
     * Logout user (invalidate token)
     * 
     * In the mainframe, logout was handled by PF3 key in COSGN00C:
     *   WHEN DFHPF3
     *       MOVE CCDA-MSG-THANK-YOU TO WS-MESSAGE
     *       PERFORM SEND-PLAIN-TEXT
     * 
     * For JWT, we rely on token expiration. A token blacklist could be
     * implemented for immediate invalidation if needed.
     * 
     * @param token JWT token to invalidate
     */
    public void logout(String token) {
        // TODO: Implement token blacklist for immediate invalidation
        // For now, tokens are invalidated by expiration
        log.info("User logged out. Token will expire naturally.");
    }

    /**
     * Custom authentication exception
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
