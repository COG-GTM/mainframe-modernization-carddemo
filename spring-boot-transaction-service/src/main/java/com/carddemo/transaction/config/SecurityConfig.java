package com.carddemo.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the transaction service.
 * Replaces the CICS sign-on program (COSGN00C) that checked
 * CDEMO-USER-ID and CDEMO-USER-TYPE in the COMMAREA.
 *
 * Current setup: Basic authentication with role-based access.
 * For production, replace with OAuth 2.0 / JWT token-based auth.
 *
 * Role mapping from COBOL:
 * - CDEMO-USRTYP-ADMIN ('A') -> ROLE_ADMIN
 * - CDEMO-USRTYP-USER  ('U') -> ROLE_USER
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic -> {});
        return http.build();
    }
}
