package com.carddemo.controller;

import com.carddemo.entity.AuthSummary;
import com.carddemo.repository.AuthSummaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/authorizations")
public class AuthorizationSummaryController {
    private final AuthSummaryRepository authSummaryRepository;

    public AuthorizationSummaryController(AuthSummaryRepository authSummaryRepository) {
        this.authSummaryRepository = authSummaryRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuthSummary>> listAuthorizations() {
        return ResponseEntity.ok(authSummaryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthSummary> getAuthorization(@PathVariable Long id) {
        return ResponseEntity.ok(authSummaryRepository.findById(id)
            .orElseThrow(() -> new com.carddemo.exception.ResourceNotFoundException("Authorization not found: " + id)));
    }
}
