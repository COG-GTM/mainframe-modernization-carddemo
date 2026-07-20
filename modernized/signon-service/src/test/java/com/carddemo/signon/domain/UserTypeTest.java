package com.carddemo.signon.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTypeTest {

    @Test
    void adminCodeMapsToAdmin() {
        assertThat(UserType.fromCode("A")).isEqualTo(UserType.ADMIN);
        assertThat(UserType.ADMIN.code()).isEqualTo('A');
    }

    @Test
    void userCodeMapsToUser() {
        assertThat(UserType.fromCode("U")).isEqualTo(UserType.USER);
        assertThat(UserType.USER.code()).isEqualTo('U');
    }

    @Test
    void anyNonAdminCodeFallsThroughToUser() {
        // Mirrors COSGN00C: IF CDEMO-USRTYP-ADMIN ... ELSE (regular user)
        assertThat(UserType.fromCode("")).isEqualTo(UserType.USER);
        assertThat(UserType.fromCode(null)).isEqualTo(UserType.USER);
        assertThat(UserType.fromCode("X")).isEqualTo(UserType.USER);
    }
}
