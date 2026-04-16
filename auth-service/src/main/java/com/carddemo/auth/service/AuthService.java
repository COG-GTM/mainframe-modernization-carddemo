package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.entity.UserEntity;
import com.carddemo.auth.exception.AuthenticationException;
import com.carddemo.auth.exception.UserNotFoundException;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service ported from COSGN00C.cbl (Sign-on Screen).
 *
 * Preserves the exact COBOL error messages:
 *   1. 'Please enter User ID ...'     (empty user ID)
 *   2. 'Please enter Password ...'    (empty password)
 *   3. 'Wrong Password. Try again ...' (password mismatch)
 *   4. 'User not found. Try again ...' (RESP=NOTFND / user not in DB)
 *   5. 'Unable to verify the User ...' (other unexpected error)
 *
 * Business rules from COBOL:
 *   - User ID and password are converted to UPPER-CASE before comparison
 *   - User type 'A' maps to admin role; 'U' maps to regular user role
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        // COBOL: MOVE FUNCTION UPPER-CASE(USERIDI) TO WS-USER-ID
        String userId = request.userId().trim().toUpperCase();
        String password = request.password().trim().toUpperCase();

        if (userId.isBlank()) {
            throw new AuthenticationException("Please enter User ID ...");
        }
        if (password.isBlank()) {
            throw new AuthenticationException("Please enter Password ...");
        }

        // COBOL: READ-USER-SEC-FILE with RESP check
        UserEntity user;
        try {
            user = userRepository.findByUserIdIgnoreCase(userId)
                    .orElseThrow(() ->
                            // COBOL RESP=13 (NOTFND): 'User not found. Try again ...'
                            new UserNotFoundException("User not found. Try again ..."));
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // COBOL RESP=OTHER: 'Unable to verify the User ...'
            throw new AuthenticationException("Unable to verify the User ...");
        }

        // COBOL: IF SEC-USR-PWD = WS-USER-PWD (case-insensitive after UPPER-CASE)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        // On success: set CDEMO-USER-ID, CDEMO-USER-TYPE, generate JWT
        String userType = user.getUserType().toUpperCase();
        String token = jwtTokenProvider.generateToken(user.getUserId(), userType);

        return new LoginResponse(
                token,
                user.getUserId(),
                userType,
                user.getFirstName(),
                user.getLastName()
        );
    }
}
