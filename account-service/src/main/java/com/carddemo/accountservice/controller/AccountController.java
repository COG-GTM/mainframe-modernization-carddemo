package com.carddemo.accountservice.controller;

import com.carddemo.accountservice.dto.AccountUpdateRequest;
import com.carddemo.accountservice.dto.AccountViewResponse;
import com.carddemo.accountservice.dto.ErrorResponse;
import com.carddemo.accountservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Account Management.
 * Ports COACTVWC.cbl (Account View) and COACTUPC.cbl (Account Update) to REST API.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account Management",
        description = "Account view and update operations ported from COBOL CardDemo programs")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "View account details",
            description = "Retrieves account details including customer information and card data. "
                    + "Ports COACTVWC.cbl Account View program logic.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account found",
                            content = @Content(schema = @Schema(implementation = AccountViewResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid account ID",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Account/customer/xref not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<AccountViewResponse> viewAccount(
            @Parameter(description = "11-digit account ID", example = "00000000001")
            @PathVariable("id") Long id) {
        AccountViewResponse response = accountService.viewAccount(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update account details",
            description = "Updates account and associated customer details. "
                    + "Ports COACTUPC.cbl Account Update program logic with full validation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account updated",
                            content = @Content(schema = @Schema(implementation = AccountViewResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation errors",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Account/customer/xref not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Concurrent modification conflict",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public ResponseEntity<AccountViewResponse> updateAccount(
            @Parameter(description = "11-digit account ID", example = "00000000001")
            @PathVariable("id") Long id,
            @RequestBody AccountUpdateRequest request) {
        AccountViewResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }
}
