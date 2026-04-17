package com.carddemo.billing.service;

import com.carddemo.billing.client.AccountServiceClient;
import com.carddemo.billing.client.CardServiceClient;
import com.carddemo.billing.client.TransactionServiceClient;
import com.carddemo.billing.dto.BillPaymentRequest;
import com.carddemo.billing.dto.BillPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga orchestrator for bill payment processing.
 *
 * Modernized from COBIL00C.cbl PROCESS-ENTER-KEY paragraph (lines 154-244).
 *
 * The original COBOL program performed these steps atomically within
 * a single CICS unit of work:
 *   1. READ CXACAIX file (card cross-reference lookup)
 *   2. WRITE TRANSACT file (create transaction record)
 *   3. COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT
 *   4. REWRITE ACCTDAT file (update account balance)
 *
 * In the microservices architecture, these operations span multiple
 * services, so we implement a saga pattern with compensating transactions:
 *   Step 1: Look up card XREF from Card Service
 *   Step 2: POST transaction to Transaction Service
 *   Step 3: PUT account balance to Account Service
 *   Compensation: If step 3 fails, reverse the transaction from step 2
 *
 * Transaction field mappings from COBIL00C.cbl lines 218-232:
 *   TRAN-TYPE-CD      = '02' (bill payment) -> mapped to "BP" per spec
 *   TRAN-CAT-CD       = 2
 *   TRAN-SOURCE       = 'POS TERM' -> mapped to "BILLING" per spec
 *   TRAN-DESC         = 'BILL PAYMENT - ONLINE'
 *   TRAN-MERCHANT-ID  = 999999999
 *   TRAN-MERCHANT-NAME = 'BILL PAYMENT'
 *   TRAN-MERCHANT-CITY = 'N/A'
 *   TRAN-MERCHANT-ZIP  = 'N/A'
 */
@Component
public class BillPaymentSaga {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentSaga.class);

    private final CardServiceClient cardServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final AccountServiceClient accountServiceClient;

    public BillPaymentSaga(CardServiceClient cardServiceClient,
                           TransactionServiceClient transactionServiceClient,
                           AccountServiceClient accountServiceClient) {
        this.cardServiceClient = cardServiceClient;
        this.transactionServiceClient = transactionServiceClient;
        this.accountServiceClient = accountServiceClient;
    }

    /**
     * Execute the bill payment saga.
     *
     * @param accountId the account ID (from path parameter, maps to ACCT-ID / XREF-ACCT-ID)
     * @param request   the bill payment request containing amount and card number
     * @return the bill payment response
     */
    public Mono<BillPaymentResponse> execute(String accountId, BillPaymentRequest request) {
        log.info("Starting bill payment saga for account: {}, amount: {}", accountId, request.amount());

        // Step 1: Look up card XREF (COBIL00C.cbl line 211: PERFORM READ-CXACAIX-FILE)
        return cardServiceClient.getCardXref(accountId)
                .flatMap(xref -> {
                    // Use the card number from the request, not the XREF lookup.
                    // The XREF lookup validates the account exists and has cards.
                    String cardNum = request.cardNum();
                    log.info("Card XREF validated for account {}: using card {}", accountId, maskCardNum(cardNum));

                    // Step 2: Create transaction (COBIL00C.cbl line 233: PERFORM WRITE-TRANSACT-FILE)
                    Map<String, Object> transactionData = buildTransactionData(accountId, cardNum, request);
                    return transactionServiceClient.createTransaction(transactionData)
                            .flatMap(txnResult -> {
                                String transactionId = String.valueOf(txnResult.get("tranId"));
                                log.info("Transaction created: {}", transactionId);

                                // Step 3: Update account balance
                                // (COBIL00C.cbl line 234: COMPUTE ACCT-CURR-BAL = ACCT-CURR-BAL - TRAN-AMT)
                                // Account Service treats amount as a delta: negative = debit.
                                // For bill payment, negate the amount so it subtracts from balance.
                                BigDecimal debitAmount = request.amount().negate();

                                return accountServiceClient.updateBalance(accountId, debitAmount)
                                        .map(accountResult -> {
                                            BigDecimal updatedBalance = new BigDecimal(
                                                    String.valueOf(accountResult.getOrDefault("acctCurrBal", "0")));
                                            log.info("Account {} balance updated to {}", accountId, updatedBalance);
                                            return new BillPaymentResponse(
                                                    transactionId,
                                                    accountId,
                                                    request.amount(),
                                                    updatedBalance,
                                                    "SUCCESS",
                                                    Instant.now()
                                            );
                                        })
                                        // Compensation: reverse transaction if balance update fails
                                        .onErrorResume(balanceError -> {
                                            log.error("Balance update failed for account {}. Compensating by reversing transaction {}",
                                                    accountId, transactionId, balanceError);
                                            return transactionServiceClient.reverseTransaction(transactionId)
                                                    .then(Mono.<BillPaymentResponse>error(new RuntimeException(
                                                            "Bill payment failed: unable to update account balance. " +
                                                                    "Transaction " + transactionId + " has been reversed.",
                                                            balanceError)))
                                                    .onErrorResume(reversalError -> {
                                                        // If this is our own error from .then() above, propagate it
                                                        if (reversalError.getCause() == balanceError) {
                                                            return Mono.error(reversalError);
                                                        }
                                                        // Both balance update AND reversal failed — critical inconsistency
                                                        log.error("CRITICAL: Both balance update AND transaction reversal failed for account {} txn {}. Manual intervention required.",
                                                                accountId, transactionId, reversalError);
                                                        return Mono.error(new RuntimeException(
                                                                "Bill payment failed: balance update failed AND reversal of transaction " +
                                                                        transactionId + " also failed. Manual intervention required.",
                                                                balanceError));
                                                    });
                                        });
                            });
                });
    }

    /**
     * Build the transaction data payload.
     * Maps COBOL field assignments from COBIL00C.cbl lines 218-232.
     */
    private Map<String, Object> buildTransactionData(String accountId, String cardNum, BillPaymentRequest request) {
        Map<String, Object> data = new HashMap<>();
        // Field names must match TransactionRequest DTO in Transaction Service
        data.put("tranTypeCd", "BP");
        data.put("tranCatCd", 2);
        data.put("tranSource", "BILLING");
        data.put("tranDesc", "BILL PAYMENT - ONLINE");
        data.put("tranAmt", request.amount());
        data.put("tranMerchantId", "999999999");
        data.put("tranMerchantName", "BILL PAYMENT");
        data.put("tranMerchantCity", "N/A");
        data.put("tranMerchantZip", "N/A");
        data.put("tranCardNum", cardNum);
        data.put("tranOrigTs", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")));
        return data;
    }

    private String maskCardNum(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) {
            return "****";
        }
        return "****" + cardNum.substring(cardNum.length() - 4);
    }
}
