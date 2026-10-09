package com.carddemo.config;

import com.carddemo.repository.SecUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Replaces the CICS signon state: every API call authenticates (HTTP Basic) against USRSEC, and
 * admin-only functions (COADM01C options and JCL submission) require user type A.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/signon").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/admin/**", "/api/v1/menus/admin", "/api/v1/menus/admin/**", "/api/v1/batch/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(basic -> { })
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(SecUserRepository users) {
        return username -> users.findById(username == null ? "" : username.trim().toUpperCase())
                .map(user -> User.withUsername(user.getId())
                        .password(user.getPassword().trim())
                        .roles(user.isAdmin() ? "ADMIN" : "USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found. Try again ..."));
    }

    /** USRSEC stores the 8 byte password as entered; COSGN00C compared it uppercased. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) {
                return raw.toString().trim().toUpperCase();
            }

            @Override
            public boolean matches(CharSequence raw, String stored) {
                return stored != null && encode(raw).equals(stored.trim().toUpperCase());
            }
        };
    }
}
