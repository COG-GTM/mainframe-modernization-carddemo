package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.loader.LegacyDataLoader;
import com.carddemo.web.dto.AccountView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AccountServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private LegacyDataLoader loader;

    @BeforeEach
    void seed() {
        loader.loadAll();
    }

    @Test
    void viewJoinsAccountCustomerAndXref() {
        AccountView view = accountService.view(1L);

        assertThat(view.accountId()).isEqualTo(1L);
        assertThat(view.activeStatus()).isEqualTo("Y");
        assertThat(view.customer()).isNotNull();
        assertThat(view.customer().firstName()).isNotBlank();
        // The shipped extract carries FICO scores outside the 300-850 range COACTUPC enforces on
        // update, so only the parse itself is asserted here.
        assertThat(view.customer().ficoCreditScore()).isNotNull();
    }

    @Test
    void rejectsZeroAccountIdWithLegacyMessage() {
        assertThatThrownBy(() -> accountService.view(0L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Account number must be a non zero 11 digit number");
    }

    @Test
    void reportsMissingAccount() {
        assertThatThrownBy(() -> accountService.view(99999999999L))
                .isInstanceOf(RecordNotFoundException.class)
                .hasMessage("Did not find this account in account card xref file");
    }
}
