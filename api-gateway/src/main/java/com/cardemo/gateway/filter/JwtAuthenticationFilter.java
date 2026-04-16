package com.cardemo.gateway.filter;

import com.cardemo.gateway.enums.UserType;
import com.cardemo.gateway.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT authentication filter that extracts and validates tokens from the
 * Authorization header. Maps JWT claims to Spring Security authentication context.
 *
 * Maps COMMAREA identity pattern:
 *   CDEMO-USER-ID   → SecurityContext principal
 *   CDEMO-USER-TYPE → ROLE_ADMIN or ROLE_USER granted authority
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        extractToken(request).ifPresent(token -> {
            Optional<Claims> claimsOpt = jwtTokenProvider.validateToken(token);
            claimsOpt.ifPresent(claims -> {
                String userId = jwtTokenProvider.getUserId(claims);
                UserType userType = jwtTokenProvider.getUserType(claims);

                List<SimpleGrantedAuthority> authorities = buildAuthorities(userType);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }

    private List<SimpleGrantedAuthority> buildAuthorities(UserType userType) {
        if (userType == UserType.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
