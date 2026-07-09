package com.carddemo.web.transaction;

import org.springframework.stereotype.Component;

import com.carddemo.service.transaction.TransactionService;
import com.carddemo.service.transaction.TransactionValidationException;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CommonAction;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.transaction.dto.TransactionListResponse;

/**
 * Screen handler for {@code COTRN00C} (Transaction List) — the nav-framework adapter over
 * {@link TransactionService#list}. Reproduces the {@code COTRN00} turn:
 *
 * <ul>
 *   <li>a row selection ({@code selection=S} + {@code tranId}) XCTLs to
 *       {@code COTRN01C} ({@link CardDemoProgram#TRANSACTION_VIEW}); any other flag re-displays
 *       with "Invalid selection. Valid value is S";</li>
 *   <li>PF7/PF8 page the browse (client-driven via {@code startTranId}), emitting the
 *       "already at the top/bottom of the page" edge messages;</li>
 *   <li>otherwise the current page is re-displayed as the screen model.</li>
 * </ul>
 */
@Component
public class TransactionListScreenHandler implements ScreenHandler {

    private final TransactionService transactionService;
    private final NavigationService navigationService;

    public TransactionListScreenHandler(TransactionService transactionService,
            NavigationService navigationService) {
        this.transactionService = transactionService;
        this.navigationService = navigationService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.TRANSACTION_LIST;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        navigationService.beginTurn(commarea);

        String selection = trimToNull(request.field("selection"));
        if (selection != null) {
            if (selection.equalsIgnoreCase("S")) {
                return ScreenResult.transferTo(CardDemoProgram.TRANSACTION_VIEW);
            }
            return ScreenResult.stay(TransactionService.Messages.INVALID_SELECTION, null);
        }

        String startTranId = trimToNull(request.field("startTranId"));
        TransactionListResponse page;
        try {
            page = transactionService.list(startTranId, TransactionService.PAGE_SIZE);
        } catch (TransactionValidationException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }

        CommonAction action = request.action();
        if (action == CommonAction.PAGE_UP && startTranId == null) {
            return ScreenResult.stay(TransactionService.Messages.ALREADY_AT_TOP, page);
        }
        if (action == CommonAction.PAGE_DOWN && !page.moreRecords()) {
            return ScreenResult.stay(TransactionService.Messages.ALREADY_AT_BOTTOM, page);
        }
        return ScreenResult.stay(null, page);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
