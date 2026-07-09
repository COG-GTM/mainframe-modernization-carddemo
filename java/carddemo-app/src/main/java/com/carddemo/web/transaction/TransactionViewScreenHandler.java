package com.carddemo.web.transaction;

import org.springframework.stereotype.Component;

import com.carddemo.service.transaction.TransactionNotFoundException;
import com.carddemo.service.transaction.TransactionService;
import com.carddemo.service.transaction.TransactionValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.transaction.dto.TransactionDetailDto;

/**
 * Screen handler for {@code COTRN01C} (Transaction View) — the nav-framework adapter over
 * {@link TransactionService#view}. Reads the {@code tranId} screen field (set either by the
 * user or carried from the {@code COTRN00} row selection) and re-displays the same screen
 * with the record as the model, or with the verbatim COBOL message when the id is empty or
 * not found.
 */
@Component
public class TransactionViewScreenHandler implements ScreenHandler {

    private final TransactionService transactionService;
    private final NavigationService navigationService;

    public TransactionViewScreenHandler(TransactionService transactionService,
            NavigationService navigationService) {
        this.transactionService = transactionService;
        this.navigationService = navigationService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.TRANSACTION_VIEW;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        navigationService.beginTurn(commarea);
        try {
            TransactionDetailDto detail = transactionService.view(request.field("tranId"));
            return ScreenResult.stay(null, detail);
        } catch (TransactionValidationException | TransactionNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }
}
