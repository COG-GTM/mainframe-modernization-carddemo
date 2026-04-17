package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.TransactionEvent;
import com.carddemo.transaction.dto.TransactionRequest;
import com.carddemo.transaction.dto.TransactionResponse;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 * Tests business logic translated from COBOL programs COTRN00C, COTRN01C,
 * COTRN02C, and CBTRN02C.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher eventPublisher;

    @Mock
    private WebClient cardServiceWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository, eventPublisher, cardServiceWebClient);
    }

    @Test
    void listTransactions_noFilters() {
        Transaction t1 = createSampleTransaction("0000000000000001", "4111111111111111");
        Transaction t2 = createSampleTransaction("0000000000000002", "4222222222222222");
        Page<Transaction> page = new PageImpl<>(List.of(t1, t2));

        when(transactionRepository.findWithFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(null, null, null, 0, 10);

        assertEquals(2, result.getTotalElements());
        assertEquals("0000000000000001", result.getContent().get(0).getTranId());
        assertEquals("0000000000000002", result.getContent().get(1).getTranId());
    }

    @Test
    void listTransactions_withCardNumFilter() {
        Transaction t1 = createSampleTransaction("0000000000000001", "4111111111111111");
        Page<Transaction> page = new PageImpl<>(List.of(t1));

        when(transactionRepository.findWithFilters(eq("4111111111111111"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                "4111111111111111", null, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("4111111111111111", result.getContent().get(0).getTranCardNum());
    }

    @Test
    void listTransactions_withDateFilters() {
        Transaction t1 = createSampleTransaction("0000000000000001", "4111111111111111");
        Page<Transaction> page = new PageImpl<>(List.of(t1));

        when(transactionRepository.findWithFilters(
                isNull(), eq("2024-01-01"), eq("2024-12-31"), any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.listTransactions(
                null, "2024-01-01", "2024-12-31", 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTransactionById_found() {
        Transaction t = createSampleTransaction("0000000000000001", "4111111111111111");
        when(transactionRepository.findById("0000000000000001")).thenReturn(Optional.of(t));

        Optional<TransactionResponse> result = transactionService.getTransactionById("0000000000000001");

        assertTrue(result.isPresent());
        assertEquals("0000000000000001", result.get().getTranId());
        assertEquals(new BigDecimal("125.50"), result.get().getTranAmt());
    }

    @Test
    void getTransactionById_notFound() {
        when(transactionRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        Optional<TransactionResponse> result = transactionService.getTransactionById("9999999999999999");

        assertFalse(result.isPresent());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTransaction_success() {
        setupWebClientMock(Map.of("accountId", "0000000001"));

        TransactionRequest request = createSampleRequest();
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals("4111111111111111", response.getTranCardNum());
        assertEquals(new BigDecimal("125.50"), response.getTranAmt());

        // Verify event was published
        ArgumentCaptor<TransactionEvent> eventCaptor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(eventPublisher).publishTransactionPosted(eventCaptor.capture());
        TransactionEvent event = eventCaptor.getValue();
        assertEquals("4111111111111111", event.getCardNum());
        assertEquals("0000000001", event.getAccountId());
        assertEquals(new BigDecimal("125.50"), event.getAmount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTransaction_cardNotFound() {
        // Simulate Card Service returning 404
        when(cardServiceWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
            // First onStatus call is for 4xx - simulate error
            java.util.function.Predicate<org.springframework.http.HttpStatusCode> predicate =
                    invocation.getArgument(0);
            Function<org.springframework.web.reactive.function.client.ClientResponse, Mono<? extends Throwable>> handler =
                    invocation.getArgument(1);
            return responseSpec;
        });
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new IllegalArgumentException(
                        "Card number 9999999999999999 not found in cross-reference")));

        TransactionRequest request = createSampleRequest();
        request.setTranCardNum("9999999999999999");

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(request));

        verify(eventPublisher, never()).publishTransactionPosted(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void batchPost_allSuccess() {
        setupWebClientMock(Map.of("accountId", "0000000001"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req1 = createSampleRequest();
        TransactionRequest req2 = createSampleRequest();
        req2.setTranDesc("Second transaction");

        Map<String, Object> result = transactionService.batchPostTransactions(List.of(req1, req2));

        assertEquals(2, result.get("totalProcessed"));
        assertEquals(2, result.get("postedCount"));
        assertEquals(0, result.get("rejectedCount"));
        verify(eventPublisher, times(2)).publishTransactionPosted(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void batchPost_partialFailure() {
        // First call succeeds, second fails
        when(cardServiceWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("accountId", "0000000001")))
                .thenReturn(Mono.error(new IllegalArgumentException("Card not found")));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req1 = createSampleRequest();
        TransactionRequest req2 = createSampleRequest();
        req2.setTranCardNum("9999999999999999");

        Map<String, Object> result = transactionService.batchPostTransactions(List.of(req1, req2));

        assertEquals(2, result.get("totalProcessed"));
        assertEquals(1, result.get("postedCount"));
        assertEquals(1, result.get("rejectedCount"));
        verify(eventPublisher, times(1)).publishTransactionPosted(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTransaction_publishesEvent() {
        setupWebClientMock(Map.of("accountId", "ACC12345"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = createSampleRequest();
        transactionService.createTransaction(request);

        ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(eventPublisher).publishTransactionPosted(captor.capture());

        TransactionEvent event = captor.getValue();
        assertNotNull(event.getTransactionId());
        assertEquals("4111111111111111", event.getCardNum());
        assertEquals("ACC12345", event.getAccountId());
        assertEquals(new BigDecimal("125.50"), event.getAmount());
        assertNotNull(event.getTimestamp());
    }

    @SuppressWarnings("unchecked")
    private void setupWebClientMock(Map<String, Object> xrefResponse) {
        when(cardServiceWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(xrefResponse));
    }

    private Transaction createSampleTransaction(String tranId, String cardNum) {
        Transaction t = new Transaction();
        t.setTranId(tranId);
        t.setTranTypeCd("01");
        t.setTranCatCd(5001);
        t.setTranSource("ONLINE");
        t.setTranDesc("Test Transaction");
        t.setTranAmt(new BigDecimal("125.50"));
        t.setTranMerchantId("100000001");
        t.setTranMerchantName("Test Merchant");
        t.setTranMerchantCity("New York");
        t.setTranMerchantZip("10001");
        t.setTranCardNum(cardNum);
        t.setTranOrigTs("2024-01-15-10.30.00.000000");
        t.setTranProcTs("2024-01-15-10.30.05.000000");
        return t;
    }

    private TransactionRequest createSampleRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setTranTypeCd("01");
        request.setTranCatCd(5001);
        request.setTranSource("ONLINE");
        request.setTranDesc("Grocery Store Purchase");
        request.setTranAmt(new BigDecimal("125.50"));
        request.setTranMerchantId("100000001");
        request.setTranMerchantName("FreshMart Groceries");
        request.setTranMerchantCity("New York");
        request.setTranMerchantZip("10001");
        request.setTranCardNum("4111111111111111");
        request.setTranOrigTs("2024-01-15-10.30.00.000000");
        return request;
    }
}
