package com.carddemo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST sign-on / sign-off endpoints — the online entry point ported from {@code COSGN00C}.
 *
 * <ul>
 *   <li>{@code POST /api/auth/signon} — validates and authenticates {userId, password},
 *       establishes the session security context, and returns the user's id, name, role and
 *       destination menu (admin vs main). Failures carry the verbatim COBOL messages.</li>
 *   <li>{@code POST /api/auth/signoff} — clears the security context and ends the session.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signon")
    public SignonResponse signon(@RequestBody SignonRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        return authService.signon(request, httpRequest, httpResponse);
    }

    @PostMapping("/signoff")
    public ResponseEntity<Void> signoff(HttpServletRequest httpRequest) {
        authService.signoff(httpRequest);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(SignonException.class)
    public ResponseEntity<SignonErrorResponse> handleSignonException(SignonException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new SignonErrorResponse(ex.getMessage()));
    }
}
