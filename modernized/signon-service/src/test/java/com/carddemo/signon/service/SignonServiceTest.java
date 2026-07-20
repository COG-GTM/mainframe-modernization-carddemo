package com.carddemo.signon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.carddemo.signon.domain.UserSecurity;
import com.carddemo.signon.domain.UserSecurityRepository;
import com.carddemo.signon.domain.UserType;
import com.carddemo.signon.service.SignonResult.Outcome;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Unit tests for {@link SignonService} with a mocked repository. There is one
 * test per branch of {@code COSGN00C}'s {@code PROCESS-ENTER-KEY} /
 * {@code READ-USER-SEC-FILE}, using the README parity fixtures.
 */
class SignonServiceTest {

    private UserSecurityRepository repository;
    private SignonService service;

    private static final UserSecurity ADMIN =
            new UserSecurity("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A");
    private static final UserSecurity USER =
            new UserSecurity("USER0001", "LAWRENCE", "THOMAS", "PASSWORD", "U");

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UserSecurityRepository.class);
        service = new SignonService(repository);
    }

    @Test
    void adminCredentialsRouteToAdminMenu() {
        when(repository.findById("ADMIN001")).thenReturn(Optional.of(ADMIN));

        SignonResult result = service.authenticate("ADMIN001", "PASSWORD");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.outcome()).isEqualTo(Outcome.ADMIN_MENU);
        assertThat(result.destinationProgram()).isEqualTo("COADM01C");
        assertThat(result.userType()).isEqualTo(UserType.ADMIN);
        assertThat(result.message()).isEmpty();
    }

    @Test
    void regularCredentialsRouteToMainMenu() {
        when(repository.findById("USER0001")).thenReturn(Optional.of(USER));

        SignonResult result = service.authenticate("USER0001", "PASSWORD");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.outcome()).isEqualTo(Outcome.MAIN_MENU);
        assertThat(result.destinationProgram()).isEqualTo("COMEN01C");
        assertThat(result.userType()).isEqualTo(UserType.USER);
    }

    @Test
    void wrongPasswordIsRejected() {
        when(repository.findById("USER0001")).thenReturn(Optional.of(USER));

        SignonResult result = service.authenticate("USER0001", "WRONG");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.outcome()).isEqualTo(Outcome.WRONG_PASSWORD);
        assertThat(result.message()).isEqualTo("Wrong Password. Try again ...");
        assertThat(result.destinationProgram()).isNull();
    }

    @Test
    void unknownUserIsRejected() {
        when(repository.findById("NOSUCH")).thenReturn(Optional.empty());

        SignonResult result = service.authenticate("NOSUCH", "PASSWORD");

        assertThat(result.outcome()).isEqualTo(Outcome.USER_NOT_FOUND);
        assertThat(result.message()).isEqualTo("User not found. Try again ...");
    }

    @Test
    void blankUserIdIsRejectedBeforeLookup() {
        SignonResult result = service.authenticate("   ", "PASSWORD");

        assertThat(result.outcome()).isEqualTo(Outcome.MISSING_USER_ID);
        assertThat(result.message()).isEqualTo("Please enter User ID ...");
        Mockito.verifyNoInteractions(repository);
    }

    @Test
    void blankPasswordIsRejectedBeforeLookup() {
        SignonResult result = service.authenticate("ADMIN001", "   ");

        assertThat(result.outcome()).isEqualTo(Outcome.MISSING_PASSWORD);
        assertThat(result.message()).isEqualTo("Please enter Password ...");
        Mockito.verifyNoInteractions(repository);
    }

    @Test
    void userIdIsBlankWhenNull() {
        SignonResult result = service.authenticate(null, "PASSWORD");
        assertThat(result.outcome()).isEqualTo(Outcome.MISSING_USER_ID);
    }

    @Test
    void credentialsAreUpperCasedLikeTheMainframe() {
        // COSGN00C upper-cases user id and password before lookup/compare.
        when(repository.findById("ADMIN001")).thenReturn(Optional.of(ADMIN));

        SignonResult result = service.authenticate("admin001", "password");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.userId()).isEqualTo("ADMIN001");
        assertThat(result.outcome()).isEqualTo(Outcome.ADMIN_MENU);
    }

    @Test
    void datastoreFailureMapsToVerifyError() {
        // Equivalent to the CICS READ resp OTHER branch.
        when(repository.findById(any()))
                .thenThrow(new DataAccessResourceFailureException("boom"));

        SignonResult result = service.authenticate("ADMIN001", "PASSWORD");

        assertThat(result.outcome()).isEqualTo(Outcome.VERIFY_ERROR);
        assertThat(result.message()).isEqualTo("Unable to verify the User ...");
    }
}
