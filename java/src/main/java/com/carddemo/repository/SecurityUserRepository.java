package com.carddemo.repository;

import com.carddemo.model.entity.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS USRSEC access (READ/STARTBR/BROWSE on SEC-USR-ID). */
@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, String> {
}
