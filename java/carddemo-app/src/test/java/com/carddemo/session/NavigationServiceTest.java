package com.carddemo.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigationServiceTest {

    private NavigationService navigation;

    @BeforeEach
    void setUp() {
        navigation = new NavigationService(new ProgramRegistry());
    }

    @Test
    void transferControlSetsFromToAndEntersTarget() {
        CardDemoCommarea commarea = new CardDemoCommarea();

        navigation.transferControl(commarea, CardDemoProgram.MAIN_MENU, CardDemoProgram.ACCOUNT_VIEW);

        assertThat(commarea.getFromTranId()).isEqualTo("CM00");
        assertThat(commarea.getFromProgram()).isEqualTo("COMEN01C");
        assertThat(commarea.getToTranId()).isEqualTo("CAVW");
        assertThat(commarea.getToProgram()).isEqualTo("COACTVWC");
        assertThat(commarea.isEnter()).isTrue();
        assertThat(commarea.getProgramContext()).isEqualTo(ProgramContext.ENTER);
    }

    @Test
    void beginTurnFlipsEnterToReenterOnFirstEntryOnly() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        navigation.transferControl(commarea, CardDemoProgram.MAIN_MENU, CardDemoProgram.ACCOUNT_VIEW);

        // First entry: ENTER, then flipped to RE-ENTER for the next turn.
        assertThat(navigation.beginTurn(commarea)).isTrue();
        assertThat(commarea.isReenter()).isTrue();

        // Subsequent turn: already RE-ENTER, stays RE-ENTER.
        assertThat(navigation.beginTurn(commarea)).isFalse();
        assertThat(commarea.isReenter()).isTrue();
    }

    @Test
    void signonRoutesAdminToAdminMenuAndUserToMainMenu() {
        CardDemoCommarea admin = new CardDemoCommarea();
        CardDemoProgram adminMenu = navigation.signon(admin, "ADMIN001", UserType.ADMIN);
        assertThat(adminMenu).isEqualTo(CardDemoProgram.ADMIN_MENU);
        assertThat(admin.getToProgram()).isEqualTo("COADM01C");
        assertThat(admin.getFromProgram()).isEqualTo("COSGN00C");
        assertThat(admin.getUserId()).isEqualTo("ADMIN001");
        assertThat(admin.getUserType()).isEqualTo(UserType.ADMIN);
        assertThat(admin.isEnter()).isTrue();

        CardDemoCommarea user = new CardDemoCommarea();
        CardDemoProgram mainMenu = navigation.signon(user, "USER0001", UserType.USER);
        assertThat(mainMenu).isEqualTo(CardDemoProgram.MAIN_MENU);
        assertThat(user.getToProgram()).isEqualTo("COMEN01C");
    }

    @Test
    void backReturnsToCallerAndFallsBackToSignon() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        navigation.transferControl(commarea, CardDemoProgram.MAIN_MENU, CardDemoProgram.ACCOUNT_VIEW);
        navigation.stay(commarea);

        CardDemoProgram target = navigation.back(commarea);

        assertThat(target).isEqualTo(CardDemoProgram.MAIN_MENU);
        assertThat(commarea.getToProgram()).isEqualTo("COMEN01C");
        assertThat(commarea.getFromProgram()).isEqualTo("COACTVWC");
        assertThat(commarea.isEnter()).isTrue();

        // No known caller -> fall back to sign-on screen.
        CardDemoCommarea orphan = new CardDemoCommarea();
        orphan.getGeneralInfo().setToProgram("COMEN01C");
        assertThat(navigation.back(orphan)).isEqualTo(CardDemoProgram.SIGNON);
    }

    @Test
    void applyPfKeyReturnsBackTargetForPf3AndEmptyForEnter() {
        CardDemoCommarea commarea = new CardDemoCommarea();
        navigation.transferControl(commarea, CardDemoProgram.MAIN_MENU, CardDemoProgram.ACCOUNT_VIEW);

        assertThat(navigation.applyPfKey(commarea, PfKey.ENTER)).isEmpty();

        Optional<CardDemoProgram> backTarget = navigation.applyPfKey(commarea, PfKey.PF3);
        assertThat(backTarget).contains(CardDemoProgram.MAIN_MENU);
    }
}
