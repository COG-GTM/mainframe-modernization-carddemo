package com.carddemo.signon.service;

import com.carddemo.signon.domain.UserSecurity;
import com.carddemo.signon.domain.UserSecurityRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Signon business logic — the modernized equivalent of {@code COSGN00C}'s
 * {@code PROCESS-ENTER-KEY} and {@code READ-USER-SEC-FILE} paragraphs.
 *
 * <p>The evaluation order and outcomes are a faithful translation of the COBOL
 * source:
 * <ol>
 *   <li>blank user id → {@code MISSING_USER_ID}</li>
 *   <li>blank password → {@code MISSING_PASSWORD}</li>
 *   <li>USRSEC lookup by user id (CICS READ):
 *     <ul>
 *       <li>found &amp; password matches → route to admin/main menu by user type</li>
 *       <li>found &amp; password differs → {@code WRONG_PASSWORD}</li>
 *       <li>not found (resp 13) → {@code USER_NOT_FOUND}</li>
 *       <li>other error (resp OTHER) → {@code VERIFY_ERROR}</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>Like the mainframe program, both the user id and password are upper-cased
 * before lookup and comparison
 * ({@code MOVE FUNCTION UPPER-CASE(...) TO WS-USER-ID / WS-USER-PWD}), so
 * credential matching is case-insensitive.
 */
@Service
public class SignonService {

    private final UserSecurityRepository repository;

    public SignonService(UserSecurityRepository repository) {
        this.repository = repository;
    }

    public SignonResult authenticate(String userIdInput, String passwordInput) {
        String userId = normalize(userIdInput);
        if (userId.isEmpty()) {
            return SignonResult.failure(
                    SignonResult.Outcome.MISSING_USER_ID, SignonMessages.ENTER_USER_ID, userId);
        }
        if (isBlank(passwordInput)) {
            return SignonResult.failure(
                    SignonResult.Outcome.MISSING_PASSWORD, SignonMessages.ENTER_PASSWORD, userId);
        }
        String password = normalize(passwordInput);

        Optional<UserSecurity> found;
        try {
            found = repository.findById(userId);
        } catch (DataAccessException ex) {
            // Equivalent to the READ resp OTHER branch.
            return SignonResult.failure(
                    SignonResult.Outcome.VERIFY_ERROR, SignonMessages.VERIFY_ERROR, userId);
        }

        if (found.isEmpty()) {
            // Equivalent to the READ resp 13 (NOTFND) branch.
            return SignonResult.failure(
                    SignonResult.Outcome.USER_NOT_FOUND, SignonMessages.USER_NOT_FOUND, userId);
        }

        UserSecurity user = found.get();
        if (!normalize(user.getPassword()).equals(password)) {
            return SignonResult.failure(
                    SignonResult.Outcome.WRONG_PASSWORD, SignonMessages.WRONG_PASSWORD, userId);
        }

        if (user.isAdmin()) {
            return SignonResult.success(
                    SignonResult.Outcome.ADMIN_MENU, SignonMessages.ADMIN_PROGRAM,
                    userId, user.type());
        }
        return SignonResult.success(
                SignonResult.Outcome.MAIN_MENU, SignonMessages.MAIN_PROGRAM,
                userId, user.type());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
