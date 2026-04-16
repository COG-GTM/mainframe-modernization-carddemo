package com.cardemo.gateway.config;

import com.cardemo.gateway.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration that enforces role-based route protection.
 *
 * Maps COBOL access control patterns:
 *   CDEMO-USRTYP-ADMIN VALUE 'A' → ROLE_ADMIN (admin menu + regular menu)
 *   CDEMO-USRTYP-USER  VALUE 'U' → ROLE_USER  (regular menu only)
 *
 * Admin endpoints (COADM02Y options: COUSR00C–COUSR03C) require ROLE_ADMIN.
 * Regular endpoints require any authenticated user (ROLE_USER).
 * Auth login endpoint is public.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - auth/login (COSGN00C equivalent)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        // Health and info endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/gateway/health").permitAll()
                        .requestMatchers("/api/v1/gateway/routes").permitAll()
                        // Admin-only endpoints (COADM02Y: COUSR00C-COUSR03C)
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/menu/admin/**").hasRole("ADMIN")
                        // All other API endpoints require authentication
                        .requestMatchers("/api/v1/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
