package com.carddemo.web.account;

import java.util.Map;

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
import com.carddemo.web.account.dto.AccountUpdateRequest;
import com.carddemo.web.account.dto.AccountUpdateResponse;

/**
 * {@link ScreenHandler} for the {@code COACTUP} account-update program. Ports the
 * pseudo-conversational shell of {@code COACTUPC}: PF3 returns to the caller; otherwise the
 * account id comes from the submitted {@code acctId} field (or the selected account on the
 * commarea) and the editable fields are read from the screen request into an
 * {@link AccountUpdateRequest}, validated and applied by {@link AccountService}.
 *
 * <p>The COBOL {@code WS-RETURN-MSG}/{@code WS-INFO-MSG} text — a validation failure, the
 * "no change detected" notice or the commit confirmation — is returned as the
 * {@link ScreenResult} message, and the resulting account model is carried with it.</p>
 */
@Component
public class AccountUpdateScreenHandler implements ScreenHandler {

    private final AccountService accountService;
    private final NavigationService navigationService;

    public AccountUpdateScreenHandler(AccountService accountService, NavigationService navigationService) {
        this.accountService = accountService;
        this.navigationService = navigationService;
    }

    @Override
    public CardDemoProgram program() {
        return CardDemoProgram.ACCOUNT_UPDATE;
    }

    @Override
    public ScreenResult handle(ScreenRequest request, CardDemoCommarea commarea) {
        if (request.action() == CommonAction.BACK) {
            navigationService.back(commarea);
            return ScreenResult.back();
        }

        boolean firstEntry = navigationService.beginTurn(commarea);

        String acctId = request.field("acctId");
        if (acctId == null || acctId.isBlank()) {
            acctId = commarea.getAccountInfo().getAcctId();
        }

        // First entry only pre-loads the current values for editing (no update submitted yet).
        if (firstEntry) {
            try {
                return ScreenResult.stay(null, accountService.view(acctId));
            } catch (AccountInputException | AccountNotFoundException ex) {
                return ScreenResult.stay(ex.getMessage(), null);
            }
        }

        try {
            AccountUpdateResponse response = accountService.update(acctId, fromFields(request.fields()));
            commarea.getAccountInfo().setAcctId(response.account().acctId());
            commarea.getAccountInfo().setAcctStatus(response.account().acctActiveStatus());
            return ScreenResult.stay(response.message(), response);
        } catch (AccountInputException | AccountNotFoundException ex) {
            return ScreenResult.stay(ex.getMessage(), null);
        }
    }

    private static AccountUpdateRequest fromFields(Map<String, String> f) {
        return new AccountUpdateRequest(
            f.get("acctActiveStatus"),
            f.get("openYear"), f.get("openMonth"), f.get("openDay"),
            f.get("creditLimit"),
            f.get("expiryYear"), f.get("expiryMonth"), f.get("expiryDay"),
            f.get("cashCreditLimit"),
            f.get("reissueYear"), f.get("reissueMonth"), f.get("reissueDay"),
            f.get("currentBalance"),
            f.get("currentCycleCredit"),
            f.get("currentCycleDebit"),
            f.get("accountGroupId"),
            f.get("ssnPart1"), f.get("ssnPart2"), f.get("ssnPart3"),
            f.get("dobYear"), f.get("dobMonth"), f.get("dobDay"),
            f.get("ficoScore"),
            f.get("firstName"), f.get("middleName"), f.get("lastName"),
            f.get("addressLine1"), f.get("addressLine2"), f.get("city"),
            f.get("stateCode"), f.get("zipCode"), f.get("countryCode"),
            f.get("phone1Area"), f.get("phone1Prefix"), f.get("phone1Line"),
            f.get("phone2Area"), f.get("phone2Prefix"), f.get("phone2Line"),
            f.get("governmentIssuedId"),
            f.get("eftAccountId"),
            f.get("primaryCardHolderIndicator"));
    }
}
