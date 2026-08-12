package com.carddemo.online.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.online.transaction.dto.TransactionAddRequest;
import com.carddemo.online.transaction.dto.TransactionAddResponse;
import com.carddemo.online.transaction.dto.TransactionListRequest;
import com.carddemo.online.transaction.dto.TransactionListResponse;
import com.carddemo.online.transaction.dto.TransactionListRow;
import com.carddemo.online.transaction.dto.TransactionViewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** COBOL programs: COTRN00C / COTRN01C / COTRN02C — the REST face of the CT00/CT01/CT02 screens. */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionListService listService;

    @MockBean
    private TransactionViewService viewService;

    @MockBean
    private TransactionAddService addService;

    @Test
    void listReturnsThePageAndKeepsTheBrowsePositionInTheSession() throws Exception {
        given(listService.handle(any(), any()))
                .willAnswer(invocation -> {
                    TransactionListState state = invocation.getArgument(1);
                    state.setPageNumber(1);
                    state.setLastTransactionId("0000000000000010");
                    state.setNextPageAvailable(true);
                    return TransactionListResponse.builder()
                            .success(true)
                            .pageNumber(1)
                            .nextPageAvailable(true)
                            .transactions(List.of(TransactionListRow.builder()
                                    .transactionId("0000000000000001")
                                    .date("06/11/22")
                                    .description("TRANSACTION 1")
                                    .amount("+00000001.05")
                                    .build()))
                            .build();
                });

        mockMvc.perform(post("/api/transactions/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransactionListRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.nextPageAvailable").value(true))
                .andExpect(jsonPath("$.transactions[0].amount").value("+00000001.05"))
                .andExpect(request ->
                        org.assertj.core.api.Assertions.assertThat(request.getRequest()
                                        .getSession()
                                        .getAttribute(TransactionListState.SESSION_KEY))
                                .isNotNull());
    }

    @Test
    void aScreenErrorIsReportedAsABadRequest() throws Exception {
        given(viewService.view("0000000000009999"))
                .willReturn(TransactionViewResponse.builder()
                        .success(false)
                        .errorMessage(TransactionViewService.MSG_TRAN_ID_NOT_FOUND)
                        .build());

        mockMvc.perform(get("/api/transactions/0000000000009999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Transaction ID NOT found..."));
    }

    @Test
    void addReturnsTheGeneratedTransactionId() throws Exception {
        given(addService.add(any()))
                .willReturn(TransactionAddResponse.builder()
                        .success(true)
                        .transactionId("0000000000000010")
                        .message("Transaction added successfully.  Your Tran ID is 0000000000000010.")
                        .build());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TransactionAddRequest.builder().accountId("7").confirm("Y").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("0000000000000010"));
    }
}
