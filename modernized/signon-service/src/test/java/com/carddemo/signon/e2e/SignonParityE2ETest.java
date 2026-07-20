package com.carddemo.signon.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.signon.web.SignonRequest;
import com.carddemo.signon.web.SignonResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * End-to-end functional-parity gate. Boots the whole application on a random
 * port (seeded by {@link com.carddemo.signon.domain.UsrsecSeeder} with the
 * sample USRSEC fixtures) and drives the signon flow over real HTTP the way a
 * client would, asserting behavior equivalent to the mainframe {@code COSGN00C}:
 *
 * <ul>
 *   <li>{@code ADMIN001/PASSWORD} reaches the admin destination (COADM01C)</li>
 *   <li>{@code USER0001/PASSWORD} reaches the regular destination (COMEN01C)</li>
 *   <li>invalid credentials produce the equivalent error message and stay on the
 *       signon screen (no routing)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignonParityE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private ResponseEntity<SignonResponse> signon(String userId, String password) {
        return rest.postForEntity("/api/signon", new SignonRequest(userId, password),
                SignonResponse.class);
    }

    @Test
    void adminUserReachesAdminDestination() {
        ResponseEntity<SignonResponse> response = signon("ADMIN001", "PASSWORD");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SignonResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.outcome()).isEqualTo("ADMIN_MENU");
        assertThat(body.destinationProgram()).isEqualTo("COADM01C");
        assertThat(body.userType()).isEqualTo("ADMIN");
    }

    @Test
    void regularUserReachesMainDestination() {
        ResponseEntity<SignonResponse> response = signon("USER0001", "PASSWORD");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SignonResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.outcome()).isEqualTo("MAIN_MENU");
        assertThat(body.destinationProgram()).isEqualTo("COMEN01C");
        assertThat(body.userType()).isEqualTo("USER");
    }

    @Test
    void allSeededAdminsAndUsersAreRoutedByType() {
        // Proves the full sample USRSEC fixture set (DUSRSECJ) was loaded.
        assertThat(signon("ADMIN002", "PASSWORD").getBody().outcome()).isEqualTo("ADMIN_MENU");
        assertThat(signon("ADMIN005", "PASSWORD").getBody().outcome()).isEqualTo("ADMIN_MENU");
        assertThat(signon("USER0002", "PASSWORD").getBody().outcome()).isEqualTo("MAIN_MENU");
        assertThat(signon("USER0005", "PASSWORD").getBody().outcome()).isEqualTo("MAIN_MENU");
    }

    @Test
    void wrongPasswordStaysOnSignonScreen() {
        ResponseEntity<SignonResponse> response = signon("USER0001", "BADPWD");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        SignonResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.message()).isEqualTo("Wrong Password. Try again ...");
        assertThat(body.destinationProgram()).isNull();
    }

    @Test
    void unknownUserStaysOnSignonScreen() {
        ResponseEntity<SignonResponse> response = signon("NOSUCH99", "PASSWORD");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("User not found. Try again ...");
        assertThat(response.getBody().destinationProgram()).isNull();
    }

    @Test
    void blankCredentialsReturnMainframePrompts() {
        assertThat(signon("", "PASSWORD").getBody().message())
                .isEqualTo("Please enter User ID ...");
        assertThat(signon("ADMIN001", "").getBody().message())
                .isEqualTo("Please enter Password ...");
    }

    @Test
    void credentialsAreCaseInsensitiveLikeTheMainframe() {
        ResponseEntity<SignonResponse> response = signon("admin001", "password");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().outcome()).isEqualTo("ADMIN_MENU");
    }

    @Test
    void signonPageIsServed() {
        ResponseEntity<String> page = rest.getForEntity("/", String.class);

        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("CC00");
    }
}
