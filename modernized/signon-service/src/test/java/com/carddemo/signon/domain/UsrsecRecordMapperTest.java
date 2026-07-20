package com.carddemo.signon.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the fixed-width {@code USRSEC} record mapping derived from
 * copybook {@code CSUSR01Y}. The sample records match the in-stream data in
 * {@code app/jcl/DUSRSECJ.jcl}.
 */
class UsrsecRecordMapperTest {

    // "ADMIN001" + "MARGARET"(20) + "GOLD"(20) + "PASSWORD" + "A" + filler(23) = 80
    private static final String ADMIN_RECORD =
            "ADMIN001" + pad("MARGARET", 20) + pad("GOLD", 20) + "PASSWORD" + "A" + " ".repeat(23);
    private static final String USER_RECORD =
            "USER0001" + pad("LAWRENCE", 20) + pad("THOMAS", 20) + "PASSWORD" + "U" + " ".repeat(23);

    @Test
    void recordLengthMatchesCopybook() {
        assertThat(UsrsecRecordMapper.RECORD_LENGTH).isEqualTo(80);
        assertThat(ADMIN_RECORD).hasSize(80);
    }

    @Test
    void parsesAdminRecordIntoAllCopybookFields() {
        UserSecurity user = UsrsecRecordMapper.parse(ADMIN_RECORD);

        assertThat(user.getUserId()).isEqualTo("ADMIN001");
        assertThat(user.getFirstName()).isEqualTo("MARGARET");
        assertThat(user.getLastName()).isEqualTo("GOLD");
        assertThat(user.getPassword()).isEqualTo("PASSWORD");
        assertThat(user.getUserType()).isEqualTo("A");
        assertThat(user.type()).isEqualTo(UserType.ADMIN);
        assertThat(user.isAdmin()).isTrue();
    }

    @Test
    void parsesRegularUserRecord() {
        UserSecurity user = UsrsecRecordMapper.parse(USER_RECORD);

        assertThat(user.getUserId()).isEqualTo("USER0001");
        assertThat(user.getLastName()).isEqualTo("THOMAS");
        assertThat(user.type()).isEqualTo(UserType.USER);
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    void formatRoundTripsBackToEightyCharRecord() {
        UserSecurity user = UsrsecRecordMapper.parse(ADMIN_RECORD);

        String formatted = UsrsecRecordMapper.format(user);

        assertThat(formatted).hasSize(80);
        assertThat(UsrsecRecordMapper.parse(formatted).getUserId()).isEqualTo("ADMIN001");
        assertThat(formatted).isEqualTo(ADMIN_RECORD);
    }

    @Test
    void rejectsRecordOfWrongLength() {
        assertThatThrownBy(() -> UsrsecRecordMapper.parse("TOOSHORT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("80");
    }

    private static String pad(String value, int length) {
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
