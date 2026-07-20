package com.carddemo.signon.web;

import com.carddemo.signon.service.SignonResult;
import com.carddemo.signon.service.SignonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for the modernized signon flow — the cloud equivalent of
 * CICS transaction {@code CC00} ({@code COSGN00C}).
 *
 * <p>The HTTP status reflects the {@code COSGN00C} outcome while the body always
 * carries the verbatim mainframe message and (on success) the XCTL navigation
 * target, preserving observable behavior.
 */
@RestController
@RequestMapping("/api")
public class SignonController {

    private final SignonService signonService;

    public SignonController(SignonService signonService) {
        this.signonService = signonService;
    }

    @PostMapping("/signon")
    public ResponseEntity<SignonResponse> signon(@RequestBody SignonRequest request) {
        SignonResult result = signonService.authenticate(request.userId(), request.password());
        return ResponseEntity.status(statusFor(result.outcome()))
                .body(SignonResponse.from(result));
    }

    private static HttpStatus statusFor(SignonResult.Outcome outcome) {
        return switch (outcome) {
            case ADMIN_MENU, MAIN_MENU -> HttpStatus.OK;
            case MISSING_USER_ID, MISSING_PASSWORD -> HttpStatus.BAD_REQUEST;
            case WRONG_PASSWORD, USER_NOT_FOUND -> HttpStatus.UNAUTHORIZED;
            case VERIFY_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
}
