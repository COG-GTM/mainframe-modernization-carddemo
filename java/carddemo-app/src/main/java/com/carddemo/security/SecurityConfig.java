package com.carddemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Spring Security 6 configuration for the migrated CardDemo sign-on ({@code COSGN00C}).
 *
 * <p>Session-based (HTTP session) authentication to fit the CICS pseudo-conversational model:
 * {@code POST /api/auth/signon} authenticates and establishes a security context that is
 * persisted in the {@code HttpSession}; subsequent requests reuse it. Two roles are derived
 * from {@code SEC-USR-TYPE} (see {@link CardDemoRole}): {@code ROLE_ADMIN} ({@code 'A'}) and
 * {@code ROLE_USER} ({@code 'U'}).</p>
 *
 * <p>Public endpoints: {@code /api/health} and the sign-on endpoint {@code /api/auth/signon}.
 * Everything else requires authentication and returns {@code 401} when unauthenticated.</p>
 *
 * <p><strong>Legacy-compat password strategy:</strong> USRSEC stores 8-char plaintext
 * passwords, so the {@link PasswordEncoder} is a delegating encoder whose stored values carry
 * the {@code {noop}} prefix (applied in {@link CardDemoUserDetails#getPassword()}). This
 * preserves the exact {@code COSGN00C} plaintext comparison. It is a documented migration
 * decision; see {@code docs/mapping/CS-2-security.md} for the rehash follow-up. New encoders
 * (bcrypt, etc.) can be adopted transparently because the delegating encoder keys off the
 * {@code {id}} prefix of each stored credential.</p>
 */
@Configuration
public class SecurityConfig {

    /** Public endpoints — the health probe and the sign-on entry point. */
    static final String[] PUBLIC_ENDPOINTS = {"/api/health", "/api/auth/signon"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            SecurityContextRepository securityContextRepository) throws Exception {
        http
            // Stateless REST clients drive sign-on explicitly; CSRF cookies are not used.
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
            // Pseudo-conversational: keep server-side HTTP session state across requests.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository))
            // REST semantics: unauthenticated access to a protected resource → 401 (not the
            // servlet default 403), matching the "you are not signed on" state of COSGN00C.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // No servlet-container HTTP Basic / form login — sign-on is the REST endpoint.
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * Delegating encoder that understands the {@code {noop}} prefix used for the legacy
     * plaintext USRSEC passwords (see class doc). Chosen deliberately so future re-hashed
     * credentials with other {@code {id}} prefixes validate without config changes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // We distinguish "user not found" from "wrong password" upstream (see AuthService) to
        // reproduce the distinct COSGN00C messages, so do not hide the not-found case here.
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return provider::authenticate;
    }

    /** Persists the {@code SecurityContext} in the {@code HttpSession} between requests. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
