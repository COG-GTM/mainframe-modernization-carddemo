package com.carddemo.acctupdate;

import com.carddemo.acctupdate.api.*;
import com.carddemo.acctupdate.service.AccountUpdateService;
import com.carddemo.acctupdate.web.AccountUpdateController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountUpdateController.class)
class AccountUpdateControllerTest {
    @Autowired MockMvc mvc;
    @MockBean AccountUpdateService service;
    @Test void getMaps404() throws Exception {
        AccountUpdateResponse r = new AccountUpdateResponse(); r.setAction(ChangeAction.DETAILS_NOT_FETCHED); r.setErrorMessage(Messages.XREF_NOT_FOUND);
        when(service.readAccount("12345678901")).thenReturn(r);
        mvc.perform(get("/api/v1/accounts/12345678901")).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorMessage").value(Messages.XREF_NOT_FOUND));
    }
    @Test void getMaps400() throws Exception {
        AccountUpdateResponse r = new AccountUpdateResponse(); r.setAction(ChangeAction.DETAILS_NOT_FETCHED); r.setErrorMessage(Messages.ACCOUNT_INVALID);
        when(service.readAccount("bad")).thenReturn(r);
        mvc.perform(get("/api/v1/accounts/bad")).andExpect(status().isBadRequest());
    }
}
