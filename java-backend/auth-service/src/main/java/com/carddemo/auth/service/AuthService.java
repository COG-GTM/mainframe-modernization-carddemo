package com.carddemo.auth.service;

import com.carddemo.auth.entity.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.common.dto.AuthRequest;
import com.carddemo.common.dto.AuthResponse;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.exception.AuthenticationException;
import com.carddemo.common.exception.UserNotFoundException;
import com.carddemo.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUserId());

        User user = userRepository.findByUserId(request.getUserId().toUpperCase())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        if (!user.getPassword().equals(request.getPassword().toUpperCase())) {
            log.warn("Invalid password for user: {}", request.getUserId());
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserType());

        UserDto userDto = UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();

        log.info("User {} authenticated successfully", request.getUserId());
        return AuthResponse.of(token, jwtTokenProvider.getExpirationMs(), userDto);
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public UserDto getCurrentUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();
    }
}
