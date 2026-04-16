package com.carddemo.shared.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    // ---- UserType ----

    @Test
    void userType_adminHasCodeA() {
        assertEquals('A', UserType.ADMIN.getCode());
    }

    @Test
    void userType_userHasCodeU() {
        assertEquals('U', UserType.USER.getCode());
    }

    @Test
    void userType_fromCodeChar() {
        assertEquals(UserType.ADMIN, UserType.fromCode('A'));
        assertEquals(UserType.USER, UserType.fromCode('U'));
    }

    @Test
    void userType_fromCodeString() {
        assertEquals(UserType.ADMIN, UserType.fromCode("A"));
        assertEquals(UserType.USER, UserType.fromCode("U"));
    }

    @Test
    void userType_fromInvalidCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode('X'));
    }

    @Test
    void userType_fromInvalidStringThrows() {
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode("AB"));
        assertThrows(IllegalArgumentException.class, () -> UserType.fromCode((String) null));
    }

    // ---- ProgramContext ----

    @Test
    void programContext_enterHasCode0() {
        assertEquals(0, ProgramContext.ENTER.getCode());
    }

    @Test
    void programContext_reenterHasCode1() {
        assertEquals(1, ProgramContext.REENTER.getCode());
    }

    @Test
    void programContext_fromCode() {
        assertEquals(ProgramContext.ENTER, ProgramContext.fromCode(0));
        assertEquals(ProgramContext.REENTER, ProgramContext.fromCode(1));
    }

    @Test
    void programContext_fromInvalidCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> ProgramContext.fromCode(5));
    }

    // ---- AccountActiveStatus ----

    @Test
    void accountActiveStatus_activeHasCodeY() {
        assertEquals('Y', AccountActiveStatus.ACTIVE.getCode());
    }

    @Test
    void accountActiveStatus_inactiveHasCodeN() {
        assertEquals('N', AccountActiveStatus.INACTIVE.getCode());
    }

    @Test
    void accountActiveStatus_fromCode() {
        assertEquals(AccountActiveStatus.ACTIVE, AccountActiveStatus.fromCode('Y'));
        assertEquals(AccountActiveStatus.INACTIVE, AccountActiveStatus.fromCode('N'));
    }

    @Test
    void accountActiveStatus_fromCodeString() {
        assertEquals(AccountActiveStatus.ACTIVE, AccountActiveStatus.fromCode("Y"));
        assertEquals(AccountActiveStatus.INACTIVE, AccountActiveStatus.fromCode("N"));
    }

    @Test
    void accountActiveStatus_fromInvalidCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> AccountActiveStatus.fromCode('X'));
    }

    // ---- CardActiveStatus ----

    @Test
    void cardActiveStatus_activeHasCodeY() {
        assertEquals('Y', CardActiveStatus.ACTIVE.getCode());
    }

    @Test
    void cardActiveStatus_inactiveHasCodeN() {
        assertEquals('N', CardActiveStatus.INACTIVE.getCode());
    }

    @Test
    void cardActiveStatus_fromCode() {
        assertEquals(CardActiveStatus.ACTIVE, CardActiveStatus.fromCode('Y'));
        assertEquals(CardActiveStatus.INACTIVE, CardActiveStatus.fromCode('N'));
    }

    @Test
    void cardActiveStatus_fromCodeString() {
        assertEquals(CardActiveStatus.ACTIVE, CardActiveStatus.fromCode("Y"));
        assertEquals(CardActiveStatus.INACTIVE, CardActiveStatus.fromCode("N"));
    }

    @Test
    void cardActiveStatus_fromInvalidCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> CardActiveStatus.fromCode('Z'));
    }
}
