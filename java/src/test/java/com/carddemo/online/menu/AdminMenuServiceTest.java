package com.carddemo.online.menu;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.dto.CardDemoCommarea;
import org.junit.jupiter.api.Test;

/** Option handling of COBOL program COADM01C (copybook COADM02Y). */
class AdminMenuServiceTest {

    private final AdminMenuService service = new AdminMenuService();

    private static CardDemoCommarea adminCommarea() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("ADMIN001");
        commarea.setUserType(CardDemoCommarea.USER_TYPE_ADMIN);
        return commarea;
    }

    @Test
    void routesTheFourUserAdministrationOptions() {
        assertThat(service.processEnterKey("1", adminCommarea()).nextProgram()).isEqualTo("COUSR00C");
        assertThat(service.processEnterKey("2", adminCommarea()).nextProgram()).isEqualTo("COUSR01C");
        assertThat(service.processEnterKey("3", adminCommarea()).nextProgram()).isEqualTo("COUSR02C");
        assertThat(service.processEnterKey("4", adminCommarea()).nextProgram()).isEqualTo("COUSR03C");
    }

    @Test
    void setsTheNavigationFieldsOfTheCommarea() {
        CardDemoCommarea commarea = adminCommarea();

        MenuResponse response = service.processEnterKey("2", commarea);

        assertThat(response.errorMessage()).isNull();
        assertThat(commarea.getFromProgram()).isEqualTo("COADM01C");
        assertThat(commarea.getFromTransactionId()).isEqualTo("CA00");
        assertThat(commarea.getToTransactionId()).isEqualTo("CU01");
        assertThat(commarea.getProgramContext()).isZero();
    }

    @Test
    void rejectsAnOptionOutsideTheTable() {
        assertThat(service.processEnterKey("5", adminCommarea()).errorMessage())
                .isEqualTo("Please enter a valid option number...");
        assertThat(service.processEnterKey(" ", adminCommarea()).errorMessage())
                .isEqualTo("Please enter a valid option number...");
    }

    @Test
    void buildsTheOptionTextsOfTheScreen() {
        assertThat(service.menuScreen(adminCommarea()).options())
                .containsExactly(
                        "01. User List (Security)",
                        "02. User Add (Security)",
                        "03. User Update (Security)",
                        "04. User Delete (Security)");
    }
}
