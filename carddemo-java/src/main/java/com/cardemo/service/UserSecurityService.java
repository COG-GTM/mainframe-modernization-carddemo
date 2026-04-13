package com.cardemo.service;

import com.cardemo.entity.UserSecurity;
import com.cardemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for UserSecurity entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ (STARTBR/READNEXT/READPREV),
 * WRITE, REWRITE, DELETE.
 */
@Service
@Transactional
public class UserSecurityService {

    private final UserSecurityRepository userSecurityRepository;

    public UserSecurityService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserSecurity> findById(String userId) {
        return userSecurityRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public List<UserSecurity> findAll() {
        return userSecurityRepository.findAll();
    }

    public UserSecurity save(UserSecurity user) {
        return userSecurityRepository.save(user);
    }

    public UserSecurity update(UserSecurity user) {
        return userSecurityRepository.save(user);
    }

    /** Mirrors COBOL DELETE on USRSEC (program COUSR03C). */
    public void delete(String userId) {
        userSecurityRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<UserSecurity> findByUserType(String userType) {
        return userSecurityRepository.findBySecUsrType(userType);
    }
}
