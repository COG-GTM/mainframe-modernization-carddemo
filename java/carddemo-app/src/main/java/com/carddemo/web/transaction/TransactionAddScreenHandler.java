package com.carddemo.web.transaction;

import org.springframework.stereotype.Component;

import com.carddemo.service.transaction.TransactionService;
import com.carddemo.service.transaction.TransactionValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.transaction.dto.TransactionAddRequest;
import com.carddemo.web.transaction.dto.TransactionAddResponse;

/**
 * Screen handler for {@code COTRN02C} (Add Transaction) — the nav-framework adapter over
 * {@link TransactionService#add}. Builds the add request from the {@code COTRN02} input
 * fields and re-displays the screen ({@code SEND-TRNADD-SCREEN}) with either the success
 * message + generated id or the verbatim validation message.
 */
@Component
public class TransactionAddScreenHandler implements ScreenHandler {

    private final TransactionService transactionService;
    private final NavigationService navigationService;

    public TransactionAddScreenHandler(TransactionService transactionService,
            NavigationService navigationService) {
        this.transactionService = transactionService;
        this.navigationService = navigationService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.TRANSACTION_ADD;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        navigationService.beginTurn(commarea);
        TransactionAddRequest addRequest = new TransactionAddRequest(
                request.field("acctId"),
                request.field("cardNum"),
                request.field("typeCd"),
                request.field("categoryCd"),
                request.field("source"),
                request.field("description"),
                request.field("amount"),
                request.field("origDate"),
                request.field("procDate"),
                request.field("merchantId"),
                request.field("merchantName"),
                request.field("merchantCity"),
                request.field("merchantZip"),
                request.field("confirm"));
        try {
            TransactionAddResponse response = transactionService.add(addRequest);
            return ScreenResult.stay(response.message(), response);
        } catch (TransactionValidationException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }
}
