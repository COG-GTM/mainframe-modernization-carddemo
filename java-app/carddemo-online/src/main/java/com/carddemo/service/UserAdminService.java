package com.carddemo.service;

import com.carddemo.dto.UserCreateRequest;
import com.carddemo.dto.UserUpdateRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User administration service — replaces COUSR00C, COUSR01C, COUSR02C, COUSR03C.
 */
@Service
public class UserAdminService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserSecurityRepository userSecurityRepository,
                            PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** List users with pagination — mirrors COUSR00C. */
    public Page<UserSecurity> listUsers(Pageable pageable) {
        return userSecurityRepository.findAll(pageable);
    }

    /** Get user by ID — mirrors COUSR01C view. */
    public UserSecurity getUser(String userId) {
        return userSecurityRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /** Add user — mirrors COUSR02C. */
    @Transactional
    public UserSecurity addUser(UserCreateRequest dto) {
        if (userSecurityRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User ID already exists: " + dto.getUserId());
        }

        UserSecurity user = new UserSecurity();
        user.setUserId(dto.getUserId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserType(com.carddemo.enums.UserType.valueOf(dto.getUserType()));

        return userSecurityRepository.save(user);
    }

    /** Update user — mirrors COUSR03C update path. */
    @Transactional
    public UserSecurity updateUser(String userId, UserUpdateRequest dto) {
        UserSecurity user = userSecurityRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getUserType() != null) user.setUserType(com.carddemo.enums.UserType.valueOf(dto.getUserType()));

        return userSecurityRepository.save(user);
    }

    /** Delete user — mirrors COUSR03C delete path. */
    @Transactional
    public void deleteUser(String userId) {
        UserSecurity user = userSecurityRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        userSecurityRepository.delete(user);
    }
}
