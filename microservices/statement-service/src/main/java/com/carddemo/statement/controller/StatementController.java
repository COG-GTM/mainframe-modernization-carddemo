package com.carddemo.statement.controller;

import com.carddemo.statement.dto.StatementRequest;
import com.carddemo.statement.dto.StatementResponse;
import com.carddemo.statement.service.StatementService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for statement generation endpoints.
 * Exposes the modernized CBSTM03A functionality as a REST API.
 */
@RestController
@RequestMapping("/statements")
public class StatementController {

    private static final Logger log = LoggerFactory.getLogger(StatementController.class);

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    /**
     * Generate a statement for a given account ID and date range.
     * POST /statements/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateStatement(
            @Valid @RequestBody StatementRequest request) {
        log.info("Statement generation requested for accountId={}", request.getAccountId());
        try {
            StatementResponse response = statementService.generateStatement(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Statement generation failed for accountId={}: {}", request.getAccountId(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Statement generation failed", "message", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * GET /statements/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "statement-service"
        ));
    }
}
