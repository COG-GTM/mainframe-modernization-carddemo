package com.carddemo.web.billpay;

import org.springframework.stereotype.Component;

import com.carddemo.service.billpay.BillPayException;
import com.carddemo.service.billpay.BillPayService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.billpay.dto.BillPayRequest;
import com.carddemo.web.billpay.dto.BillPayResponse;

/**
 * {@link ScreenHandler} for the Bill Payment screen ({@code COBIL00} / {@code COBIL00C}),
 * registered against {@link CardDemoProgram#BILL_PAYMENT}. It bridges a pseudo-conversational
 * navigation turn to {@link BillPayService}: the {@code COBIL0A} input fields
 * ({@code ACTIDIN}/{@code CONFIRM}) arrive as {@link ScreenRequest} fields, and the outcome is
 * returned as a {@link ScreenResult} that keeps control on the screen ({@code RETURN
 * TRANSID('CB00')}), matching the COBOL which always re-displays {@code COBIL0A}.
 */
@Component
public class BillPayScreenHandler implements ScreenHandler {

    /** {@code ACTIDIN} — the account id input field. */
    public static final String FIELD_ACCT_ID = "acctId";
    /** {@code CONFIRM} — the {@code (Y/N)} confirmation field. */
    public static final String FIELD_CONFIRM = "confirm";

    private final BillPayService billPayService;
    private final NavigationService navigation;

    public BillPayScreenHandler(BillPayService billPayService, NavigationService navigation) {
        this.billPayService = billPayService;
        this.navigation = navigation;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.BILL_PAYMENT;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        // First entry (ENTER): display the empty screen; process input on RE-ENTRY only.
        if (navigation.beginTurn(commarea)) {
            return ScreenResult.stay();
        }
        BillPayRequest payRequest = new BillPayRequest(
                request.field(FIELD_ACCT_ID), request.field(FIELD_CONFIRM));
        try {
            BillPayResponse response = billPayService.pay(payRequest);
            return ScreenResult.stay(response.message(), response);
        } catch (BillPayException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }
}
