package com.carddemo.online.menu;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.dto.CardDemoCommarea;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Option handling of COBOL program COMEN01C (copybook COMEN02Y). */
class MainMenuServiceTest {

    private final MainMenuService service = new MainMenuService();

    private static CardDemoCommarea commarea(String userType) {
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("USER0001");
        commarea.setUserType(userType);
        return commarea;
    }

    @Test
    void routesOptionThreeToTheCreditCardList() {
        CardDemoCommarea commarea = commarea(CardDemoCommarea.USER_TYPE_USER);

        MenuResponse response = service.processEnterKey("3", commarea);

        assertThat(response.nextProgram()).isEqualTo("COCRDLIC");
        assertThat(response.errorMessage()).isNull();
        assertThat(commarea.getFromProgram()).isEqualTo("COMEN01C");
        assertThat(commarea.getFromTransactionId()).isEqualTo("CM00");
        assertThat(commarea.getToProgram()).isEqualTo("COCRDLIC");
        assertThat(commarea.getProgramContext()).isZero();
    }

    @Test
    void acceptsATrailingBlankAndATwoDigitOption() {
        assertThat(service.processEnterKey("1 ", commarea("U")).nextProgram()).isEqualTo("COACTVWC");
        assertThat(service.processEnterKey("10", commarea("U")).nextProgram()).isEqualTo("COBIL00C");
    }

    @Test
    void rejectsOptionsOutsideTheTable() {
        for (String option : List.of("", "0", "11", "AB")) {
            MenuResponse response = service.processEnterKey(option, commarea("U"));
            assertThat(response.errorMessage())
                    .as("option '%s'", option)
                    .isEqualTo("Please enter a valid option number...");
            assertThat(response.nextProgram()).isNull();
        }
    }

    @Test
    void deniesAnAdminOnlyOptionToARegularUser() {
        MainMenuService menu = new MainMenuService(
                List.of(new MenuOption(1, "Admin Reports", "CORPT99C", "A")));

        MenuResponse denied = menu.processEnterKey("1", commarea(CardDemoCommarea.USER_TYPE_USER));
        assertThat(denied.errorMessage()).isEqualTo("No access - Admin Only option... ");
        assertThat(denied.nextProgram()).isNull();

        MenuResponse allowed = menu.processEnterKey("1", commarea(CardDemoCommarea.USER_TYPE_ADMIN));
        assertThat(allowed.nextProgram()).isEqualTo("CORPT99C");
    }

    @Test
    void reportsAnUnimplementedOptionWithTheCobolWording() {
        MainMenuService menu = new MainMenuService(
                List.of(new MenuOption(1, "Account View", "DUMMY   ", "U")));

        assertThat(menu.processEnterKey("1", commarea("U")).errorMessage())
                .isEqualTo("This option Accountis coming soon ...");
    }

    @Test
    void buildsTheOptionTextsOfTheScreen() {
        MenuResponse response = service.menuScreen(commarea("U"));

        assertThat(response.options()).hasSize(10);
        assertThat(response.options().get(0)).isEqualTo("01. Account View");
        assertThat(response.options().get(9)).isEqualTo("10. Bill Payment");
    }

    @Test
    void pf3GoesBackToTheSignonProgram() {
        CardDemoCommarea commarea = commarea("U");

        MenuResponse response = service.processPf3Key(commarea);

        assertThat(response.nextProgram()).isEqualTo("COSGN00C");
        assertThat(commarea.getToTransactionId()).isEqualTo("CC00");
    }

    @Test
    void anyOtherKeyIsRejected() {
        assertThat(service.processOtherKey().errorMessage())
                .isEqualTo("Invalid key pressed. Please see below...");
    }
}
