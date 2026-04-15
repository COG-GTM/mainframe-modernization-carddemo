package com.carddemo.payment.controller;

import com.carddemo.payment.dto.PaymentRequest;
import com.carddemo.payment.dto.PaymentResponse;
import com.carddemo.payment.model.Account;
import com.carddemo.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Check account balance before payment.
     * GET /api/payments/balance/{accountId}
     */
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long accountId) {
        Account account = paymentService.getAccountBalance(accountId);
        return ResponseEntity.ok(Map.of(
                "accountId", account.getAcctId(),
                "currentBalance", account.getCurrentBalance(),
                "activeStatus", account.getActiveStatus()
        ));
    }

    /**
     * Process a bill payment.
     * POST /api/payments/bill
     */
    @PostMapping("/bill")
    public ResponseEntity<PaymentResponse> processBillPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processBillPayment(request);
        return ResponseEntity.ok(response);
    }
}
