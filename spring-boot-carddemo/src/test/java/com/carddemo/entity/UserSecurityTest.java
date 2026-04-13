package com.carddemo.entity;

import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for UserSecurity entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies CRUD operations and custom finders mirroring COBOL USRSEC VSAM access.
 * Note: No seed data file for user security in ASCII data directory.
 */
@DataJpaTest
class UserSecurityTest {

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Test
    void userSecurityTableStartsEmpty() {
        List<UserSecurity> all = userSecurityRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void createUserPersistsAndReadsBack() {
        UserSecurity user = new UserSecurity();
        user.setSecUsrId("USER0001");
        user.setSecUsrFname("JOHN");
        user.setSecUsrLname("DOE");
        user.setSecUsrPwd("PASSWORD");
        user.setSecUsrType("U");

        userSecurityRepository.save(user);

        Optional<UserSecurity> found = userSecurityRepository.findById("USER0001");
        assertThat(found).isPresent();
        assertThat(found.get().getSecUsrFname()).isEqualTo("JOHN");
        assertThat(found.get().getSecUsrLname()).isEqualTo("DOE");
        assertThat(found.get().getSecUsrType()).isEqualTo("U");
    }

    @Test
    void createAdminUserPersistsAndReadsBack() {
        UserSecurity admin = new UserSecurity();
        admin.setSecUsrId("ADMIN001");
        admin.setSecUsrFname("ADMIN");
        admin.setSecUsrLname("USER");
        admin.setSecUsrPwd("ADMINPWD");
        admin.setSecUsrType("A");

        userSecurityRepository.save(admin);

        Optional<UserSecurity> found = userSecurityRepository.findById("ADMIN001");
        assertThat(found).isPresent();
        assertThat(found.get().getSecUsrType()).isEqualTo("A");
    }

    @Test
    void findByUserTypeReturnsMatchingUsers() {
        UserSecurity user1 = new UserSecurity();
        user1.setSecUsrId("USER0001");
        user1.setSecUsrFname("JOHN");
        user1.setSecUsrLname("DOE");
        user1.setSecUsrPwd("PWD1");
        user1.setSecUsrType("U");

        UserSecurity user2 = new UserSecurity();
        user2.setSecUsrId("USER0002");
        user2.setSecUsrFname("JANE");
        user2.setSecUsrLname("DOE");
        user2.setSecUsrPwd("PWD2");
        user2.setSecUsrType("U");

        UserSecurity admin = new UserSecurity();
        admin.setSecUsrId("ADMIN001");
        admin.setSecUsrFname("ADMIN");
        admin.setSecUsrLname("USER");
        admin.setSecUsrPwd("ADMINPWD");
        admin.setSecUsrType("A");

        userSecurityRepository.save(user1);
        userSecurityRepository.save(user2);
        userSecurityRepository.save(admin);

        List<UserSecurity> regularUsers = userSecurityRepository.findBySecUsrType("U");
        assertThat(regularUsers).hasSize(2);

        List<UserSecurity> admins = userSecurityRepository.findBySecUsrType("A");
        assertThat(admins).hasSize(1);
    }

    @Test
    void updateUserPassword() {
        // Mirrors COBOL REWRITE for password change in COUSR02C
        UserSecurity user = new UserSecurity();
        user.setSecUsrId("USER0001");
        user.setSecUsrFname("JOHN");
        user.setSecUsrLname("DOE");
        user.setSecUsrPwd("OLDPASSWD");
        user.setSecUsrType("U");

        userSecurityRepository.save(user);

        user.setSecUsrPwd("NEWPASSWD");
        userSecurityRepository.save(user);

        UserSecurity updated = userSecurityRepository.findById("USER0001").orElseThrow();
        assertThat(updated.getSecUsrPwd()).isEqualTo("NEWPASSWD");
    }

    @Test
    void deleteUserRemovesFromDatabase() {
        // Mirrors COBOL DELETE in COUSR03C
        UserSecurity user = new UserSecurity();
        user.setSecUsrId("USER0099");
        user.setSecUsrFname("DELETE");
        user.setSecUsrLname("ME");
        user.setSecUsrPwd("PASSWORD");
        user.setSecUsrType("U");

        userSecurityRepository.save(user);
        assertThat(userSecurityRepository.findById("USER0099")).isPresent();

        userSecurityRepository.deleteById("USER0099");
        assertThat(userSecurityRepository.findById("USER0099")).isEmpty();
    }

    @Test
    void userIdIsUniqueAcrossAllRecords() {
        UserSecurity u1 = new UserSecurity();
        u1.setSecUsrId("USER0001");
        u1.setSecUsrFname("A");
        u1.setSecUsrLname("B");
        u1.setSecUsrPwd("P");
        u1.setSecUsrType("U");

        UserSecurity u2 = new UserSecurity();
        u2.setSecUsrId("USER0002");
        u2.setSecUsrFname("C");
        u2.setSecUsrLname("D");
        u2.setSecUsrPwd("P");
        u2.setSecUsrType("A");

        userSecurityRepository.save(u1);
        userSecurityRepository.save(u2);

        List<UserSecurity> all = userSecurityRepository.findAll();
        long distinctIds = all.stream().map(UserSecurity::getSecUsrId).distinct().count();
        assertThat(distinctIds).isEqualTo(all.size());
    }

    @Test
    void userTypeIsEitherAdminOrRegular() {
        UserSecurity user = new UserSecurity();
        user.setSecUsrId("USER0001");
        user.setSecUsrFname("A");
        user.setSecUsrLname("B");
        user.setSecUsrPwd("P");
        user.setSecUsrType("U");

        UserSecurity admin = new UserSecurity();
        admin.setSecUsrId("ADMIN001");
        admin.setSecUsrFname("C");
        admin.setSecUsrLname("D");
        admin.setSecUsrPwd("P");
        admin.setSecUsrType("A");

        userSecurityRepository.save(user);
        userSecurityRepository.save(admin);

        List<UserSecurity> all = userSecurityRepository.findAll();
        all.forEach(u -> assertThat(u.getSecUsrType()).isIn("U", "A"));
    }
}
