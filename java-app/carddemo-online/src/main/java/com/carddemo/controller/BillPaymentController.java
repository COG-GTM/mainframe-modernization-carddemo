package com.carddemo.controller;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.service.BillPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Bill payment controller — replaces COBIL00C CICS transaction.
 */
@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping("/{acctId}/bill-payment")
    public ResponseEntity<DailyTransaction> payBill(@PathVariable Long acctId) {
        return ResponseEntity.ok(billPaymentService.payBill(acctId));
    }
}
