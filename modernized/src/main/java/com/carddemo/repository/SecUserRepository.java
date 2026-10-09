package com.carddemo.repository;

import com.carddemo.domain.SecUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecUserRepository extends JpaRepository<SecUser, String> {
}
