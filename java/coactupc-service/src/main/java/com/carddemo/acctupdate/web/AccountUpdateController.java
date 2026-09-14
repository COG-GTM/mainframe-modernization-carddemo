package com.carddemo.acctupdate.web;

import com.carddemo.acctupdate.api.*;
import com.carddemo.acctupdate.service.AccountUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountUpdateController {
    private final AccountUpdateService service;
    public AccountUpdateController(AccountUpdateService service) { this.service = service; }

    @GetMapping("/{acctId}")
    public ResponseEntity<AccountUpdateResponse> get(@PathVariable String acctId) {
        AccountUpdateResponse r = service.readAccount(acctId);
        if (r.getAction() == ChangeAction.DETAILS_NOT_FETCHED && Messages.ACCOUNT_NOT_PROVIDED.equals(r.getErrorMessage()) || r.getAction() == ChangeAction.DETAILS_NOT_FETCHED && Messages.ACCOUNT_INVALID.equals(r.getErrorMessage())) return ResponseEntity.badRequest().body(r);
        if (r.getAction() == ChangeAction.DETAILS_NOT_FETCHED) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(r);
        return ResponseEntity.ok(r);
    }
    @PostMapping("/{acctId}/validate")
    public ResponseEntity<AccountUpdateResponse> validate(@PathVariable String acctId, @RequestBody AccountUpdateRequest request) { return ResponseEntity.ok(service.editMapInputs(request)); }
    @PutMapping("/{acctId}")
    public ResponseEntity<AccountUpdateResponse> put(@PathVariable String acctId, @RequestBody AccountUpdateRequest request) {
        AccountUpdateResponse r = service.writeProcessing(request);
        if (r.getAction() == ChangeAction.CHANGES_OKAYED_LOCK_ERROR) return ResponseEntity.status(423).body(r);
        if (r.getAction() == ChangeAction.SHOW_DETAILS && Messages.DATA_CHANGED.equals(r.getErrorMessage())) return ResponseEntity.status(409).body(r);
        if (r.getAction() == ChangeAction.CHANGES_OKAYED_BUT_FAILED) return ResponseEntity.status(500).body(r);
        return ResponseEntity.ok(r);
    }
}
