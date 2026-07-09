package com.carddemo.web.useradmin;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.session.CardDemoProgram;
import com.carddemo.session.ScreenRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the four user-maintenance screen handlers ({@code COUSR00}/{@code 01}/{@code 02}/
 * {@code 03}) are discovered and registered with the CS-3 {@link ScreenRegistry}.
 */
@SpringBootTest
@ActiveProfiles("test")
class UserAdminScreenHandlerTest {

    @Autowired
    private ScreenRegistry screens;

    @Test
    void allFourUserProgramsHaveHandlers() {
        assertThat(screens.hasHandler(CardDemoProgram.USER_LIST)).isTrue();
        assertThat(screens.hasHandler(CardDemoProgram.USER_ADD)).isTrue();
        assertThat(screens.hasHandler(CardDemoProgram.USER_UPDATE)).isTrue();
        assertThat(screens.hasHandler(CardDemoProgram.USER_DELETE)).isTrue();
    }
}
