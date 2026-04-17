package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.TransactionEvent;
import com.carddemo.transaction.dto.TransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import com.carddemo.transaction.util.DateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Service implementing business logic from COBOL programs:
 * - COTRN00C: List transactions (with pagination/filtering)
 * - COTRN01C: View transaction by ID
 * - COTRN02C: Add new transaction (with card validation via XREF)
 * - CBTRN02C: Batch post daily transactions (validate & post)
 * - CBTRN03C: Transaction detail report (via filtered listing)
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final DateTimeFormatter PROC_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;
    private final WebClient cardServiceWebClient;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionEventPublisher eventPublisher,
                              WebClient cardServiceWebClient) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.cardServiceWebClient = cardServiceWebClient;
    }

    /**
     * List transactions with optional filters.
     * Translates COTRN00C STARTBR/READNEXT browse logic with pagination.
     */
    public Page<TransactionResponse> listTransactions(String cardNum, String startDate,
                                                       String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("tranId").ascending());
        Page<Transaction> transactions = transactionRepository.findWithFilters(
                cardNum, startDate, endDate, pageable);
        return transactions.map(TransactionResponse::fromEntity);
    }

    /**
     * Get a transaction by ID.
     * Translates COTRN01C READ TRANSACT-FILE by TRAN-ID.
     */
    public Optional<TransactionResponse> getTransactionById(String tranId) {
        return transactionRepository.findById(tranId)
                .map(TransactionResponse::fromEntity);
    }

    /**
     * Create a new transaction.
     * Translates COTRN02C: validate card via XREF, write transaction, publish event.
     */
    public TransactionResponse createTransaction(TransactionRequest request) {
        return createTransaction(request, true);
    }

    /**
     * Create a new transaction with optional event publishing.
     *
     * @param request   the transaction data
     * @param publishEvent whether to publish a transaction.posted event to RabbitMQ.
     *                     Set to false when the caller (e.g. BillPaymentSaga) will
     *                     handle the account balance update itself, to avoid
     *                     double-updating the balance.
     */
    public TransactionResponse createTransaction(TransactionRequest request, boolean publishEvent) {
        // Validate date fields (translating CSUTLDTC.cbl date validation)
        validateDates(request);

        // Validate card exists via Card Service (translating XREF lookup from COTRN02C)
        Map<String, Object> xrefData = validateCardNumber(request.getTranCardNum());
        String accountId = extractAccountId(xrefData);

        // Generate transaction ID if not provided
        String tranId = request.getTranId();
        if (tranId == null || tranId.isBlank()) {
            tranId = generateTransactionId();
        }

        // Generate processing timestamp
        String procTs = LocalDateTime.now().format(PROC_TS_FORMAT);

        Transaction transaction = mapRequestToEntity(request, tranId, procTs);
        Transaction saved = transactionRepository.save(transaction);

        // Publish event for Account Service (replaces CBTRN02C ADD DALYTRAN-AMT TO ACCT-CURR-BAL)
        // Skip event when caller manages balance updates directly (e.g. BillPaymentSaga)
        if (publishEvent) {
            TransactionEvent event = new TransactionEvent(
                    saved.getTranId(),
                    saved.getTranCardNum(),
                    accountId,
                    saved.getTranAmt(),
                    saved.getTranProcTs()
            );
            eventPublisher.publishTransactionPosted(event);
        }

        log.info("Transaction created: id={}, cardNum={}, amount={}",
                saved.getTranId(), saved.getTranCardNum(), saved.getTranAmt());

        return TransactionResponse.fromEntity(saved);
    }

    /**
     * Batch post daily transactions.
     * Translates CBTRN02C batch logic: read daily transactions, validate each,
     * post valid ones, reject invalid ones.
     */
    public Map<String, Object> batchPostTransactions(List<TransactionRequest> requests) {
        List<TransactionResponse> posted = new ArrayList<>();
        List<Map<String, Object>> rejected = new ArrayList<>();
        int processedCount = 0;

        for (TransactionRequest request : requests) {
            processedCount++;
            try {
                TransactionResponse response = createTransaction(request);
                posted.add(response);
            } catch (Exception e) {
                Map<String, Object> rejection = new LinkedHashMap<>();
                rejection.put("index", processedCount - 1);
                rejection.put("tranId", request.getTranId());
                rejection.put("cardNum", request.getTranCardNum());
                rejection.put("reason", e.getMessage());
                rejected.add(rejection);
                log.warn("Batch transaction rejected at index {}: {}", processedCount - 1, e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalProcessed", processedCount);
        result.put("postedCount", posted.size());
        result.put("rejectedCount", rejected.size());
        result.put("posted", posted);
        result.put("rejected", rejected);

        log.info("Batch post complete: processed={}, posted={}, rejected={}",
                processedCount, posted.size(), rejected.size());

        return result;
    }

    private void validateDates(TransactionRequest request) {
        if (request.getTranOrigTs() != null && !request.getTranOrigTs().isBlank()) {
            String datePart = request.getTranOrigTs().length() >= 10
                    ? request.getTranOrigTs().substring(0, 10)
                    : request.getTranOrigTs();
            if (!DateValidator.isValidDate(datePart)) {
                throw new IllegalArgumentException(
                        "Orig Date should be in format YYYY-MM-DD");
            }
        }
        if (request.getTranProcTs() != null && !request.getTranProcTs().isBlank()) {
            String datePart = request.getTranProcTs().length() >= 10
                    ? request.getTranProcTs().substring(0, 10)
                    : request.getTranProcTs();
            if (!DateValidator.isValidDate(datePart)) {
                throw new IllegalArgumentException(
                        "Proc Date should be in format YYYY-MM-DD");
            }
        }
    }

    /**
     * Validate card number by calling Card Service's card detail endpoint.
     * Translates COTRN02C READ-CCXREF-FILE and CBTRN02C 1500-A-LOOKUP-XREF.
     *
     * Uses GET /cards/{cardNum} which returns card details including the
     * account ID (cardAcctId field). This validates the card exists and
     * provides the account ID for the transaction event.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> validateCardNumber(String cardNum) {
        try {
            Map<String, Object> response = cardServiceWebClient.get()
                    .uri("/cards/{cardNum}", cardNum)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> Mono.error(new IllegalArgumentException(
                                    "Card number " + cardNum + " not found")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new RuntimeException(
                                    "Card Service unavailable")))
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new IllegalArgumentException(
                        "Card number " + cardNum + " not found");
            }
            return response;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Card Service unavailable")) {
                throw e;
            }
            log.warn("Card Service unreachable, proceeding with card validation skipped: {}",
                    e.getMessage());
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("accountId", "UNKNOWN");
            return fallback;
        }
    }

    private String extractAccountId(Map<String, Object> cardData) {
        // Card detail endpoint returns cardAcctId for the account ID
        Object accountId = cardData.get("cardAcctId");
        if (accountId == null) {
            accountId = cardData.get("accountId");
        }
        if (accountId == null) {
            accountId = cardData.get("acctId");
        }
        return accountId != null ? accountId.toString() : "UNKNOWN";
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private Transaction mapRequestToEntity(TransactionRequest request, String tranId, String procTs) {
        Transaction transaction = new Transaction();
        transaction.setTranId(tranId);
        transaction.setTranTypeCd(request.getTranTypeCd());
        transaction.setTranCatCd(request.getTranCatCd());
        transaction.setTranSource(request.getTranSource());
        transaction.setTranDesc(request.getTranDesc());
        transaction.setTranAmt(request.getTranAmt());
        transaction.setTranMerchantId(request.getTranMerchantId());
        transaction.setTranMerchantName(request.getTranMerchantName());
        transaction.setTranMerchantCity(request.getTranMerchantCity());
        transaction.setTranMerchantZip(request.getTranMerchantZip());
        transaction.setTranCardNum(request.getTranCardNum());
        transaction.setTranOrigTs(request.getTranOrigTs());
        transaction.setTranProcTs(procTs);
        return transaction;
    }
}
