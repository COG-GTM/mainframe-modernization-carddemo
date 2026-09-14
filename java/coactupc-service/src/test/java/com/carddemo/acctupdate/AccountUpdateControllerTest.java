package com.carddemo.acctupdate;

import com.carddemo.acctupdate.api.AccountUpdateDetails;
import com.carddemo.acctupdate.api.AccountUpdateResponse;
import com.carddemo.acctupdate.api.ChangeAction;
import com.carddemo.acctupdate.api.Messages;
import com.carddemo.acctupdate.service.AccountUpdateService;
import com.carddemo.acctupdate.web.AccountUpdateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountUpdateController.class)
class AccountUpdateControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private AccountUpdateService service;

    @Test
    void getReturnsDetails() throws Exception {
        AccountUpdateDetails details = new AccountUpdateDetails();
        details.setAcctId("12345678901");
        AccountUpdateResponse response = response(ChangeAction.SHOW_DETAILS, details, null);
        when(service.readAccount("12345678901")).thenReturn(response);

        mvc.perform(get("/api/v1/accounts/12345678901"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("SHOW_DETAILS"))
            .andExpect(jsonPath("$.details.acctId").value("12345678901"));
    }

    @Test
    void getMapsXrefNotFoundTo404() throws Exception {
        AccountUpdateResponse response = response(
            ChangeAction.DETAILS_NOT_FETCHED,
            null,
            Messages.XREF_NOT_FOUND
        );
        when(service.readAccount("12345678901")).thenReturn(response);

        mvc.perform(get("/api/v1/accounts/12345678901"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorMessage").value(Messages.XREF_NOT_FOUND));
    }

    @Test
    void getMapsInvalidIdTo400() throws Exception {
        AccountUpdateResponse response = response(
            ChangeAction.DETAILS_NOT_FETCHED,
            null,
            Messages.ACCOUNT_INVALID
        );
        when(service.readAccount("bad")).thenReturn(response);

        mvc.perform(get("/api/v1/accounts/bad"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage").value(Messages.ACCOUNT_INVALID));
    }

    @Test
    void postValidateReturns200() throws Exception {
        when(service.editMapInputs(any())).thenReturn(
            response(ChangeAction.CHANGES_OK_NOT_CONFIRMED, null, null)
        );

        mvc.perform(
                post("/api/v1/accounts/12345678901/validate")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("CHANGES_OK_NOT_CONFIRMED"));
    }

    @Test
    void putReturns200ForCompletedUpdate() throws Exception {
        when(service.writeProcessing(any())).thenReturn(
            response(ChangeAction.CHANGES_OKAYED_AND_DONE, null, null)
        );

        mvc.perform(
                put("/api/v1/accounts/12345678901")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("CHANGES_OKAYED_AND_DONE"));
    }

    @Test
    void putMapsDataChangedTo409() throws Exception {
        when(service.writeProcessing(any())).thenReturn(
            response(ChangeAction.SHOW_DETAILS, null, Messages.DATA_CHANGED)
        );

        mvc.perform(
                put("/api/v1/accounts/12345678901")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorMessage").value(Messages.DATA_CHANGED));
    }

    @Test
    void putMapsLockErrorTo423() throws Exception {
        when(service.writeProcessing(any())).thenReturn(
            response(ChangeAction.CHANGES_OKAYED_LOCK_ERROR, null, Messages.LOCK_ACCOUNT)
        );

        mvc.perform(
                put("/api/v1/accounts/12345678901")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().is(423))
            .andExpect(jsonPath("$.errorMessage").value(Messages.LOCK_ACCOUNT));
    }

    @Test
    void putMapsPersistenceFailureTo500() throws Exception {
        when(service.writeProcessing(any())).thenReturn(
            response(ChangeAction.CHANGES_OKAYED_BUT_FAILED, null, Messages.UPDATE_FAILED)
        );

        mvc.perform(
                put("/api/v1/accounts/12345678901")
                    .contentType("application/json")
                    .content("{}")
            )
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage").value(Messages.UPDATE_FAILED));
    }

    private AccountUpdateResponse response(
        ChangeAction action,
        AccountUpdateDetails details,
        String errorMessage
    ) {
        AccountUpdateResponse response = new AccountUpdateResponse();
        response.setAction(action);
        response.setDetails(details);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
