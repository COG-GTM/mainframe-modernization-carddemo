package com.carddemo.web.billpay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.billpay.BillPayException;
import com.carddemo.service.billpay.BillPayService;
import com.carddemo.web.billpay.dto.BillPayErrorResponse;
import com.carddemo.web.billpay.dto.BillPayRequest;
import com.carddemo.web.billpay.dto.BillPayResponse;

/**
 * REST bill-payment endpoint ported from {@code COBIL00C}.
 *
 * <p>{@code POST /api/billpay} pays an account balance in full: submit {@code accountId} with
 * no {@code confirm} to see the balance and be prompted to confirm, then resubmit with
 * {@code confirm=Y} to make the payment (create the payment transaction and zero the balance).
 * Rejections carry the verbatim {@code COBIL00C} messages.</p>
 */
@RestController
@RequestMapping("/api/billpay")
public class BillPayController {

    private final BillPayService billPayService;

    public BillPayController(BillPayService billPayService) {
        this.billPayService = billPayService;
    }

    @PostMapping
    public BillPayResponse pay(@RequestBody BillPayRequest request) {
        return billPayService.pay(request);
    }

    @ExceptionHandler(BillPayException.class)
    public ResponseEntity<BillPayErrorResponse> handleBillPayException(BillPayException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new BillPayErrorResponse(ex.getMessage()));
    }
}
