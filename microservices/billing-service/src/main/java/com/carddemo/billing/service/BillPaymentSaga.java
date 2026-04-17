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
                    String cardNum = (String) xref.get("cardNum");
                    log.info("Card XREF found for account {}: card {}", accountId, maskCardNum(cardNum));

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
                                                    .then(Mono.error(new RuntimeException(
                                                            "Bill payment failed: unable to update account balance. " +
                                                                    "Transaction " + transactionId + " has been reversed.",
                                                            balanceError)));
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
        data.put("accountId", accountId);
        data.put("typeCd", "BP");
        data.put("catCd", 2);
        data.put("source", "BILLING");
        data.put("description", "BILL PAYMENT - ONLINE");
        data.put("amount", request.amount());
        data.put("merchantId", 999999999);
        data.put("merchantName", "BILL PAYMENT");
        data.put("merchantCity", "N/A");
        data.put("merchantZip", "N/A");
        data.put("cardNum", cardNum);
        return data;
    }

    private String maskCardNum(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) {
            return "****";
        }
        return "****" + cardNum.substring(cardNum.length() - 4);
    }
}
