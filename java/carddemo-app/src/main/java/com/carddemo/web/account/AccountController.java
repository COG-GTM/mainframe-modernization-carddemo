package com.carddemo.web.account;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carddemo.service.account.AccountInputException;
import com.carddemo.service.account.AccountNotFoundException;
import com.carddemo.service.account.AccountService;
import com.carddemo.web.account.dto.AccountErrorResponse;
import com.carddemo.web.account.dto.AccountUpdateRequest;
import com.carddemo.web.account.dto.AccountUpdateResponse;
import com.carddemo.web.account.dto.AccountViewResponse;

/**
 * REST facade for the CardDemo account online functions ({@code COACTVWC} view /
 * {@code COACTUPC} update).
 *
 * <ul>
 *   <li>{@code GET /api/accounts/{acctId}} — view the account plus its associated customer.</li>
 *   <li>{@code PUT /api/accounts/{acctId}} — validate and apply field updates.</li>
 * </ul>
 *
 * <p>Exception handling is intentionally local to this controller (no global handler) so
 * the COBOL {@code WS-RETURN-MSG} text surfaces unchanged: {@link AccountNotFoundException}
 * → 404 and {@link AccountInputException} → 400, each carrying the original message.</p>
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{acctId}")
    public AccountViewResponse view(@PathVariable String acctId) {
        return accountService.view(acctId);
    }

    @PutMapping("/{acctId}")
    public AccountUpdateResponse update(@PathVariable String acctId,
            @RequestBody AccountUpdateRequest request) {
        return accountService.update(acctId, request);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<AccountErrorResponse> handleNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new AccountErrorResponse(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(AccountInputException.class)
    public ResponseEntity<AccountErrorResponse> handleInput(AccountInputException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new AccountErrorResponse(ex.getMessage(), ex.errors()));
    }
}
