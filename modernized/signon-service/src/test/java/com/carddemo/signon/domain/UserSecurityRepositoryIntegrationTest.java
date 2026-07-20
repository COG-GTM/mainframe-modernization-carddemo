package com.carddemo.signon.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Integration test for the USRSEC datastore against a real (H2) JPA context.
 * Verifies the keyed lookup that replaces {@code EXEC CICS READ} and that every
 * {@code CSUSR01Y} field round-trips through persistence.
 */
@DataJpaTest
class UserSecurityRepositoryIntegrationTest {

    @Autowired
    private UserSecurityRepository repository;

    @Test
    void savesAndLooksUpUserByKeyWithAllFieldsMapped() {
        repository.save(new UserSecurity("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A"));

        Optional<UserSecurity> found = repository.findById("ADMIN001");

        assertThat(found).isPresent();
        UserSecurity user = found.get();
        assertThat(user.getFirstName()).isEqualTo("MARGARET");
        assertThat(user.getLastName()).isEqualTo("GOLD");
        assertThat(user.getPassword()).isEqualTo("PASSWORD");
        assertThat(user.getUserType()).isEqualTo("A");
        assertThat(user.isAdmin()).isTrue();
    }

    @Test
    void missingKeyReturnsEmptyLikeCicsResp13() {
        assertThat(repository.findById("NOSUCH99")).isEmpty();
    }
}
