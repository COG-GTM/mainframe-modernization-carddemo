package com.carddemo.service;

import com.carddemo.dto.UserSecurityRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserSecurityRepository userSecurityRepository, PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserSecurity> listUsers(Pageable pageable) {
        return userSecurityRepository.findAll(pageable);
    }

    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserSecurity createUser(UserSecurityRequest request) {
        UserSecurity user = new UserSecurity();
        user.setUsrId(request.getUsrId());
        user.setUsrFname(request.getUsrFname());
        user.setUsrLname(request.getUsrLname());
        user.setUsrPwd(passwordEncoder.encode(request.getUsrPwd()));
        user.setUsrType(request.getUsrType());
        return userSecurityRepository.save(user);
    }

    @Transactional
    public UserSecurity updateUser(String userId, UserSecurityRequest request) {
        UserSecurity user = getUser(userId);
        if (request.getUsrFname() != null) user.setUsrFname(request.getUsrFname());
        if (request.getUsrLname() != null) user.setUsrLname(request.getUsrLname());
        if (request.getUsrPwd() != null) user.setUsrPwd(passwordEncoder.encode(request.getUsrPwd()));
        if (request.getUsrType() != null) user.setUsrType(request.getUsrType());
        return userSecurityRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        UserSecurity user = getUser(userId);
        userSecurityRepository.delete(user);
    }
}
