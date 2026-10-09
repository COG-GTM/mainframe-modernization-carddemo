package com.carddemo.web;

import com.carddemo.service.BillPaymentService;
import com.carddemo.web.dto.BillPaymentRequest;
import com.carddemo.web.dto.BillPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** COBIL00C - bill payment. */
@RestController
@RequestMapping("/api/v1/bill-payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillPaymentResponse pay(@RequestBody BillPaymentRequest request) {
        return billPaymentService.pay(request);
    }
}
