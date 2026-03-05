package com.carddemo.config;

import com.carddemo.dto.SessionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Session configuration — stores COMMAREA-equivalent SessionContext per HTTP session.
 * Mirrors CICS COMMAREA state passing between programs.
 */
@Configuration
public class SessionConfig {

    @Bean
    @SessionScope
    public SessionContext sessionContext() {
        return new SessionContext();
    }
}
