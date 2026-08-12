package com.carddemo.online.billpay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COBIL00C (transaction CB00) — bill payment screen, BMS map COBIL0A.
 *
 * <p>One request per 3270 interaction: sending the account id without a confirmation returns the
 * balance and the confirm prompt, sending it again with {@code confirm = "Y"} commits the payment.
 */
@RestController
@RequestMapping("/api/billpay")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @PostMapping
    public ResponseEntity<BillPaymentResponse> pay(@RequestBody BillPaymentRequest request) {
        BillPaymentResponse response = billPaymentService.pay(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
