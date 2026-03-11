package com.carddemo.service;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.BusinessException;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserSecurityRepository userSecurityRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserSecurityRepository userSecurityRepository,
                                  JwtTokenProvider jwtTokenProvider,
                                  PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserSecurity user = userSecurityRepository.findById(request.getUserId().toUpperCase())
            .orElseThrow(() -> new BusinessException("AUTH001", "Invalid user ID or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getUsrPwd())) {
            throw new BusinessException("AUTH001", "Invalid user ID or password");
        }

        String role = "A".equals(user.getUsrType()) ? "ADMIN" : "USER";
        String token = jwtTokenProvider.createToken(user.getUsrId(), role);
        return new LoginResponse(token, role, user.getUsrFname(), user.getUsrLname());
    }
}
