package com.carddemo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Sign-on / sign-off service — the Java port of the {@code COSGN00C} sign-on program.
 *
 * <p>Reproduces, in order, the COBOL {@code PROCESS-ENTER-KEY} + {@code READ-USER-SEC-FILE}
 * logic:</p>
 * <ol>
 *   <li>User id required — else {@link SignonMessages#EMPTY_USER_ID}.</li>
 *   <li>Password required — else {@link SignonMessages#EMPTY_PASSWORD}.</li>
 *   <li>Upper-case both inputs ({@code FUNCTION UPPER-CASE}).</li>
 *   <li>Look up USRSEC by id — missing record → {@link SignonMessages#USER_NOT_FOUND}
 *       ({@code WHEN 13}).</li>
 *   <li>Compare password — mismatch → {@link SignonMessages#WRONG_PASSWORD}.</li>
 *   <li>On success establish the security context in the HTTP session and route by role
 *       (admin menu vs main menu).</li>
 * </ol>
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CardDemoUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    public AuthService(AuthenticationManager authenticationManager,
            CardDemoUserDetailsService userDetailsService,
            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Authenticates the request and, on success, establishes a session-backed security context.
     *
     * @throws SignonException with the verbatim {@code COSGN00C} message and matching HTTP status
     */
    public SignonResponse signon(SignonRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String rawUserId = request == null ? null : request.userId();
        String rawPassword = request == null ? null : request.password();

        if (isBlank(rawUserId)) {
            throw new SignonException(SignonMessages.EMPTY_USER_ID, HttpStatus.BAD_REQUEST);
        }
        if (isBlank(rawPassword)) {
            throw new SignonException(SignonMessages.EMPTY_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        String userId = rawUserId.trim().toUpperCase(Locale.ROOT);
        String password = rawPassword.trim().toUpperCase(Locale.ROOT);

        // COBOL WHEN 13 (record not found) is distinct from the wrong-password branch, so probe
        // the store first to produce the correct message.
        try {
            userDetailsService.loadUserByUsername(userId);
        } catch (UsernameNotFoundException e) {
            throw new SignonException(SignonMessages.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, password));
        } catch (BadCredentialsException e) {
            throw new SignonException(SignonMessages.WRONG_PASSWORD, HttpStatus.UNAUTHORIZED);
        } catch (UsernameNotFoundException e) {
            throw new SignonException(SignonMessages.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            throw new SignonException(SignonMessages.UNABLE_TO_VERIFY,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return SignonResponse.from((CardDemoUserDetails) authentication.getPrincipal());
    }

    /**
     * Signs the current user off: clears the security context and invalidates the HTTP session
     * (the web equivalent of the {@code PF3} "thank you / end session" path in {@code COSGN00C}).
     */
    public void signoff(HttpServletRequest httpRequest) {
        securityContextHolderStrategy.clearContext();
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
