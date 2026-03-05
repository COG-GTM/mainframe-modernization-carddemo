package com.carddemo.authorization;

import com.carddemo.authorization.AuthorizationProcessor.AuthorizationRequest;
import com.carddemo.authorization.AuthorizationProcessor.AuthorizationResponse;
import com.carddemo.entity.AuthorizationSummary;
import com.carddemo.repository.AuthorizationSummaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Authorization view and management endpoints.
 * Translates COPAUS0C, COPAUS1C (view history), COPAUS2C (mark fraud).
 */
@RestController
@RequestMapping("/api/authorizations")
public class AuthorizationController {

    private final AuthorizationSummaryRepository authSummaryRepository;
    private final AuthorizationProcessor authorizationProcessor;

    public AuthorizationController(AuthorizationSummaryRepository authSummaryRepository,
                                    AuthorizationProcessor authorizationProcessor) {
        this.authSummaryRepository = authSummaryRepository;
        this.authorizationProcessor = authorizationProcessor;
    }

    @GetMapping
    public ResponseEntity<Page<AuthorizationSummary>> listAuthorizations(
            @RequestParam String cardNum, Pageable pageable) {
        return ResponseEntity.ok(authSummaryRepository.findByCardNum(cardNum, pageable));
    }

    @GetMapping("/{authId}")
    public ResponseEntity<AuthorizationSummary> getAuthorization(@PathVariable Long authId) {
        return ResponseEntity.ok(
                authSummaryRepository.findById(authId)
                        .orElseThrow(() -> new IllegalArgumentException("Authorization not found: " + authId))
        );
    }

    @PutMapping("/{authId}/fraud")
    public ResponseEntity<Map<String, String>> markFraud(@PathVariable Long authId) {
        AuthorizationSummary summary = authSummaryRepository.findById(authId)
                .orElseThrow(() -> new IllegalArgumentException("Authorization not found: " + authId));
        summary.setDeclineReason("Marked as fraud by admin");
        authSummaryRepository.save(summary);
        return ResponseEntity.ok(Map.of("status", "Authorization marked as fraud"));
    }

    @PostMapping
    public ResponseEntity<AuthorizationResponse> processAuthorization(@RequestBody Map<String, String> request) {
        AuthorizationRequest authRequest = new AuthorizationRequest(
                request.get("cardNum"),
                new BigDecimal(request.get("transactionAmount")),
                request.get("merchantId"),
                request.getOrDefault("authType", "PURCHASE")
        );
        return ResponseEntity.ok(authorizationProcessor.processAuthorization(authRequest));
    }
}
