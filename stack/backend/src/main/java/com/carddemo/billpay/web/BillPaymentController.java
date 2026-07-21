package com.carddemo.billpay.web;

import com.carddemo.billpay.service.BillPaymentResult;
import com.carddemo.billpay.service.BillPaymentService;
import com.carddemo.billpay.web.dto.BillPaymentResponse;
import com.carddemo.billpay.web.dto.InquiryRequest;
import com.carddemo.billpay.web.dto.PayRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for the Bill Payment transaction (CB00 / COBIL00C).
 * {@code /inquiry} = ENTER with confirm blank (show balance + prompt);
 * {@code /pay} = ENTER with the entered confirm value (full payment path).
 */
@RestController
@RequestMapping("/api/billpay")
@CrossOrigin
public class BillPaymentController {

    private final BillPaymentService service;

    public BillPaymentController(BillPaymentService service) {
        this.service = service;
    }

    @PostMapping("/inquiry")
    public BillPaymentResponse inquiry(@RequestBody InquiryRequest request) {
        BillPaymentResult result = service.inquiry(request.getAcctId());
        return BillPaymentResponse.from(result);
    }

    @PostMapping("/pay")
    public BillPaymentResponse pay(@RequestBody PayRequest request) {
        BillPaymentResult result = service.process(request.getAcctId(), request.getConfirm());
        return BillPaymentResponse.from(result);
    }
}
