package com.carddemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline security configuration for WAVE 0.
 *
 * <p>The public health endpoint is left open; everything else requires authentication.
 * Later waves extend this (form login / session handling mirroring the CICS sign-on flow,
 * role-based access from the USRSEC file, etc.).</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated())
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
