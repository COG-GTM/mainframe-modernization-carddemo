package com.carddemo.web.account;

import org.springframework.stereotype.Component;

import com.carddemo.service.account.AccountInputException;
import com.carddemo.service.account.AccountNotFoundException;
import com.carddemo.service.account.AccountService;
import com.carddemo.session.CardDemoCommarea;
import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.CommonAction;
import com.carddemo.session.NavigationService;
import com.carddemo.session.ScreenHandler;
import com.carddemo.session.ScreenRequest;
import com.carddemo.session.ScreenResult;
import com.carddemo.web.account.dto.AccountViewResponse;

/**
 * {@link ScreenHandler} for the {@code COACTVW} account-view program. Ports the
 * pseudo-conversational shell of {@code COACTVWC}: PF3 returns to the caller; otherwise the
 * account id comes from the submitted {@code acctId} field, falling back to the selected
 * account carried on the commarea ({@code CDEMO-ACCT-ID}, e.g. drilled in from a list).
 *
 * <p>On a successful read it stores the account id/status back on the commarea (so a
 * subsequent {@code XCTL} to account update inherits it) and stays on the screen with the
 * view model; the COBOL {@code WS-RETURN-MSG} texts surface as the {@link ScreenResult}
 * message when a validation or not-found condition re-displays the screen.</p>
 */
@Component
public class AccountViewScreenHandler implements ScreenHandler {

    private final AccountService accountService;
    private final NavigationService navigationService;

    public AccountViewScreenHandler(AccountService accountService, NavigationService navigationService) {
        this.accountService = accountService;
        this.navigationService = navigationService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.ACCOUNT_VIEW;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (request.action() == CommonAction.BACK) {
            navigationService.back(commarea);
            return ScreenResult.back();
        }

        navigationService.beginTurn(commarea);

        String acctId = request.field("acctId");
        if (acctId == null || acctId.isBlank()) {
            acctId = commarea.getAccountInfo().getAcctId();
        }

        try {
            AccountViewResponse response = accountService.view(acctId);
            commarea.getAccountInfo().setAcctId(response.acctId());
            commarea.getAccountInfo().setAcctStatus(response.acctActiveStatus());
            return ScreenResult.stay(null, response);
        } catch (AccountInputException | AccountNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }
}
