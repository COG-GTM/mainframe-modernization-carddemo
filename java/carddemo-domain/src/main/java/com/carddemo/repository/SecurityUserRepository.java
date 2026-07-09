package com.carddemo.repository;

import com.carddemo.domain.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, String> {
}
