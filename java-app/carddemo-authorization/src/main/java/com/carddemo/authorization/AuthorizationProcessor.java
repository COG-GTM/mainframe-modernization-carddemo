package com.carddemo.authorization;

import com.carddemo.entity.*;
import com.carddemo.enums.AuthorizationDeclineReason;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Authorization processor — translates COPAUA0C.cbl authorization logic.
 * 
 * Decision sequence:
 * 1. Parse authorization request
 * 2. Validate card via CardXrefRepository
 * 3. Validate account status (active, not closed)
 * 4. Check available credit (balance vs limit)
 * 5. Check fraud flags
 * 6. Approve or decline with reason code
 * 7. Store AuthorizationSummary and AuthorizationDetail (replaces IMS DL/I ISRT)
 * 8. If fraud detected, store AuthorizationFraud record (replaces DB2 INSERT)
 */
@Component
public class AuthorizationProcessor {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationProcessor.class);

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final AuthorizationSummaryRepository authSummaryRepository;
    private final AuthorizationFraudRepository authFraudRepository;

    public AuthorizationProcessor(CardXrefRepository cardXrefRepository,
                                   AccountRepository accountRepository,
                                   CardRepository cardRepository,
                                   AuthorizationSummaryRepository authSummaryRepository,
                                   AuthorizationFraudRepository authFraudRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.authSummaryRepository = authSummaryRepository;
        this.authFraudRepository = authFraudRepository;
    }

    @Transactional
    public AuthorizationResponse processAuthorization(AuthorizationRequest request) {
        String cardNum = request.cardNum();
        BigDecimal amount = request.transactionAmount();
        String merchantId = request.merchantId();

        // Step 2: Validate card via CardXref
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(cardNum);
        if (xrefOpt.isEmpty()) {
            return decline(request, AuthorizationDeclineReason.CARD_NOT_ACTIVE, "Card not found in system");
        }

        // Check card is active
        Optional<Card> cardOpt = cardRepository.findById(cardNum);
        if (cardOpt.isEmpty() || !"Y".equals(cardOpt.get().getActiveStatus())) {
            return decline(request, AuthorizationDeclineReason.CARD_NOT_ACTIVE, "Card is not active");
        }

        CardXref xref = xrefOpt.get();
        Long acctId = xref.getAcctId();

        // Step 3: Validate account status
        Optional<Account> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            return decline(request, AuthorizationDeclineReason.ACCOUNT_CLOSED, "Account not found");
        }

        Account account = acctOpt.get();
        if (!"Y".equals(account.getActiveStatus())) {
            return decline(request, AuthorizationDeclineReason.ACCOUNT_CLOSED, "Account is closed/inactive");
        }

        // Step 4: Check available credit
        BigDecimal currentBalance = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal creditLimit = account.getCreditLimit() != null ? account.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal availableCredit = creditLimit.subtract(currentBalance);

        if (amount.compareTo(availableCredit) > 0) {
            return decline(request, AuthorizationDeclineReason.INSUFFICIENT_FUNDS, "Insufficient credit available");
        }

        // Step 5: Check fraud flags
        List<AuthorizationFraud> fraudRecords = authFraudRepository.findByCardNum(cardNum);
        boolean cardFraud = fraudRecords.stream().anyMatch(f -> "Y".equals(f.getAuthFraudFlag()));
        if (cardFraud) {
            return decline(request, AuthorizationDeclineReason.CARD_FRAUD, "Card flagged for fraud");
        }

        // Step 6: Approve
        return approve(request, amount);
    }

    private AuthorizationResponse approve(AuthorizationRequest request, BigDecimal approvedAmount) {
        AuthorizationSummary summary = createSummary(request, "APPROVED", null, approvedAmount);
        addDetail(summary, "APPROVAL", approvedAmount, "Authorization approved");

        log.info("Authorization APPROVED: card={}, amount={}", request.cardNum(), approvedAmount);
        return new AuthorizationResponse(true, "APPROVED", null, approvedAmount, summary.getAuthId());
    }

    private AuthorizationResponse decline(AuthorizationRequest request, AuthorizationDeclineReason reason, String message) {
        AuthorizationSummary summary = createSummary(request, "DECLINED", reason.getDescription(), BigDecimal.ZERO);
        addDetail(summary, "DECLINE", BigDecimal.ZERO, reason.getDescription());

        // If fraud, store AuthorizationFraud record
        if (reason == AuthorizationDeclineReason.CARD_FRAUD || reason == AuthorizationDeclineReason.MERCHANT_FRAUD) {
            AuthorizationFraud fraud = new AuthorizationFraud();
            fraud.setCardNum(request.cardNum());
            fraud.setAuthTimestamp(LocalDateTime.now());
            fraud.setAuthType(request.authType());
            fraud.setTransactionAmt(request.transactionAmount());
            fraud.setApprovedAmt(BigDecimal.ZERO);
            fraud.setMerchantId(request.merchantId());
            fraud.setAuthFraudFlag("Y");
            authFraudRepository.save(fraud);
        }

        log.info("Authorization DECLINED: card={}, reason={}", request.cardNum(), reason);
        return new AuthorizationResponse(false, "DECLINED", reason.getCode() + ": " + reason.getDescription(), BigDecimal.ZERO, summary.getAuthId());
    }

    private AuthorizationSummary createSummary(AuthorizationRequest request, String status, String declineReason, BigDecimal approvedAmt) {
        AuthorizationSummary summary = new AuthorizationSummary();
        summary.setCardNum(request.cardNum());
        summary.setAuthTimestamp(LocalDateTime.now());
        summary.setAuthType(request.authType());
        summary.setTransactionAmt(request.transactionAmount());
        summary.setApprovedAmt(approvedAmt);
        summary.setAuthStatus(status);
        summary.setDeclineReason(declineReason);
        summary.setMerchantId(request.merchantId());
        return authSummaryRepository.save(summary);
    }

    private void addDetail(AuthorizationSummary summary, String type, BigDecimal amount, String description) {
        AuthorizationDetail detail = new AuthorizationDetail();
        detail.setDetailType(type);
        detail.setDetailAmount(amount);
        detail.setDetailTimestamp(LocalDateTime.now());
        detail.setDetailDescription(description);
        summary.addDetail(detail);
        authSummaryRepository.save(summary);
    }

    public record AuthorizationRequest(String cardNum, BigDecimal transactionAmount, String merchantId, String authType) {}
    public record AuthorizationResponse(boolean approved, String status, String declineReason, BigDecimal approvedAmount, Long authId) {}
}
