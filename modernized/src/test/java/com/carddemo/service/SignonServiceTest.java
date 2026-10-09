package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.AbstractIntegrationTest;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.loader.LegacyDataLoader;
import com.carddemo.web.dto.SignonRequest;
import com.carddemo.web.dto.SignonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SignonServiceTest extends AbstractIntegrationTest {

    @Autowired
    private SignonService signonService;
    @Autowired
    private LegacyDataLoader loader;

    @BeforeEach
    void seedUsers() {
        loader.loadUsers();
    }

    @Test
    void routesRegularUserToMainMenu() {
        SignonResponse response = signonService.signon(new SignonRequest("user0001", "password"));

        assertThat(response.userId()).isEqualTo("USER0001");
        assertThat(response.admin()).isFalse();
        assertThat(response.legacyMenuProgram()).isEqualTo("COMEN01C");
    }

    @Test
    void routesAdminToAdminMenu() {
        SignonResponse response = signonService.signon(new SignonRequest("ADMIN001", "PASSWORD"));

        assertThat(response.admin()).isTrue();
        assertThat(response.legacyMenuProgram()).isEqualTo("COADM01C");
    }

    @Test
    void preservesLegacyFailureMessages() {
        assertThatThrownBy(() -> signonService.signon(new SignonRequest("", "PASSWORD")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Please enter User ID ...");
        assertThatThrownBy(() -> signonService.signon(new SignonRequest("USER0001", "")))
                .hasMessage("Please enter Password ...");
        assertThatThrownBy(() -> signonService.signon(new SignonRequest("NOBODY01", "PASSWORD")))
                .hasMessage("User not found. Try again ...");
        assertThatThrownBy(() -> signonService.signon(new SignonRequest("USER0001", "WRONG")))
                .hasMessage("Wrong Password. Try again ...");
    }
}
