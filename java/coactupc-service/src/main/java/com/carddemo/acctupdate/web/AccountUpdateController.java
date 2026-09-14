package com.carddemo.acctupdate.web;

import com.carddemo.acctupdate.api.AccountUpdateRequest;
import com.carddemo.acctupdate.api.AccountUpdateResponse;
import com.carddemo.acctupdate.api.ChangeAction;
import com.carddemo.acctupdate.api.Messages;
import com.carddemo.acctupdate.service.AccountUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountUpdateController {
    private final AccountUpdateService service;

    public AccountUpdateController(AccountUpdateService service) {
        this.service = service;
    }

    @GetMapping("/{acctId}")
    public ResponseEntity<AccountUpdateResponse> get(@PathVariable String acctId) {
        AccountUpdateResponse response = service.readAccount(acctId);
        if (isBadAccountId(response)) {
            return ResponseEntity.badRequest().body(response);
        }
        if (response.getAction() == ChangeAction.DETAILS_NOT_FETCHED) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{acctId}/validate")
    public ResponseEntity<AccountUpdateResponse> validate(
        @PathVariable String acctId,
        @RequestBody AccountUpdateRequest request
    ) {
        return ResponseEntity.ok(service.editMapInputs(request));
    }

    @PutMapping("/{acctId}")
    public ResponseEntity<AccountUpdateResponse> put(
        @PathVariable String acctId,
        @RequestBody AccountUpdateRequest request
    ) {
        AccountUpdateResponse response = service.writeProcessing(request);
        if (response.getAction() == ChangeAction.CHANGES_OKAYED_LOCK_ERROR) {
            return ResponseEntity.status(423).body(response);
        }
        if (response.getAction() == ChangeAction.SHOW_DETAILS
            && Messages.DATA_CHANGED.equals(response.getErrorMessage())) {
            return ResponseEntity.status(409).body(response);
        }
        if (response.getAction() == ChangeAction.CHANGES_OKAYED_BUT_FAILED) {
            return ResponseEntity.status(500).body(response);
        }
        return ResponseEntity.ok(response);
    }

    private boolean isBadAccountId(AccountUpdateResponse response) {
        return response.getAction() == ChangeAction.DETAILS_NOT_FETCHED
            && (Messages.ACCOUNT_NOT_PROVIDED.equals(response.getErrorMessage())
                || Messages.ACCOUNT_INVALID.equals(response.getErrorMessage()));
    }
}
