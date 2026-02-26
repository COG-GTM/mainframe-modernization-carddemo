package com.carddemo.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the transaction service.
 *
 * Replaces the COBOL COMMAREA-based user type checks:
 *   - CDEMO-USRTYP-ADMIN (value 'A')
 *   - CDEMO-USRTYP-USER  (value 'U')
 *
 * In the original COTRN02C, the program was accessible to all user types
 * (menu option 8 has USRTYPE = 'U'). This configuration mirrors that
 * by allowing authenticated access to all transaction endpoints.
 *
 * For production, this should be extended with JWT-based authentication
 * to replace the CICS sign-on (COSGN00C) flow.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/transactions/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
