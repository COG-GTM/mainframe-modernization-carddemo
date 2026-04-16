package com.carddemo.auth.repository;

import com.carddemo.auth.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByUserIdIgnoreCase(String userId);

    Page<UserEntity> findAllByOrderByUserIdAsc(Pageable pageable);
}
