package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.entity.Transaction;
import com.carddemo.service.BillPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class BillPaymentController {
    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    public ResponseEntity<Transaction> processPayment(@Valid @RequestBody BillPaymentRequest request) {
        return ResponseEntity.ok(billPaymentService.processPayment(request));
    }
}
