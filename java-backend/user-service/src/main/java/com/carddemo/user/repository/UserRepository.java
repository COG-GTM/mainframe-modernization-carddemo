package com.carddemo.user.repository;

import com.carddemo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    Page<User> findByUserIdContainingIgnoreCase(String userId, Pageable pageable);
    Page<User> findByUserType(String userType, Pageable pageable);
}
