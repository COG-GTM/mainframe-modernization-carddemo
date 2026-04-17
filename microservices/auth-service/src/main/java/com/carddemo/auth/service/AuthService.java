package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.entity.User;
import com.carddemo.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service - modernized from COSGN00C.cbl READ-USER-SEC-FILE paragraph.
 *
 * Original COBOL flow:
 *   1. READ USRSEC file by WS-USER-ID (RESP 0 = found, 13 = not found)
 *   2. Compare SEC-USR-PWD with WS-USER-PWD
 *   3. On match: set COMMAREA fields, XCTL to COADM01C (admin) or COMEN01C (regular)
 *   4. On mismatch: send error message
 */
@Service
public class AuthService {

    private static final String ADMIN_TYPE = "A";
    private static final String ADMIN_REDIRECT = "COADM01C";
    private static final String USER_REDIRECT = "COMEN01C";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Authenticate a user and return a JWT token with user claims.
     *
     * @param request the login request containing userId and password
     * @return LoginResponse with JWT token and user info
     * @throws AuthenticationException if credentials are invalid
     */
    public LoginResponse authenticate(LoginRequest request) {
        String userId = request.getUserId().toUpperCase().trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found. Try again ..."));

        if (!passwordEncoder.matches(request.getPassword(), user.getUsrPwd())) {
            throw new AuthenticationException("Wrong Password. Try again ...");
        }

        String token = jwtService.generateToken(user);
        String redirectProgram = ADMIN_TYPE.equals(user.getUsrType()) ? ADMIN_REDIRECT : USER_REDIRECT;

        return new LoginResponse(
                token,
                user.getUsrId(),
                user.getUsrType(),
                user.getUsrFname(),
                user.getUsrLname(),
                redirectProgram
        );
    }

    /**
     * Authentication exception matching the COBOL error messages.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
