package com.carddemo.service;

import com.carddemo.dto.SessionContext;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication service — replaces COSGN00C.cbl sign-on logic.
 * Validates credentials against UserSecurity table, populates SessionContext.
 */
@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserSecurityRepository userSecurityRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate user and populate session context.
     * Mirrors COSGN00C: validate user ID exists, check password match,
     * then route based on user type (ADMIN → admin menu, USER → standard menu).
     */
    public AuthResult authenticate(String userId, String password) {
        Optional<UserSecurity> userOpt = userSecurityRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return new AuthResult(false, null, "User ID not found");
        }

        UserSecurity user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new AuthResult(false, null, "Invalid password");
        }

        return new AuthResult(true, user, null);
    }

    public record AuthResult(boolean success, UserSecurity user, String errorMessage) {}
}
