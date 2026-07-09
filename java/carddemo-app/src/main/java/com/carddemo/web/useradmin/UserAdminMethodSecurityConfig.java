package com.carddemo.web.useradmin;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables Spring Security method-level authorization so the {@link UserAdminController}
 * {@code @PreAuthorize("hasRole('ADMIN')")} guards are enforced.
 *
 * <p>All four user-maintenance programs ({@code COUSR00C}/{@code 01C}/{@code 02C}/{@code 03C})
 * are reached only from the admin menu {@code COADM01C} — they are admin-only. Rather than
 * touch the CS-2 {@code SecurityConfig} filter chain, CS-9 owns this additive
 * {@code @EnableMethodSecurity} configuration; it changes no existing endpoint (no other
 * controller uses method annotations).</p>
 */
@Configuration
@EnableMethodSecurity
public class UserAdminMethodSecurityConfig {
}
