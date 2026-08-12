package com.carddemo.repository;

import com.carddemo.model.entity.SecurityUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS USRSEC access (READ/STARTBR/BROWSE on SEC-USR-ID). */
@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUser, String> {

    /** STARTBR GTEQ on SEC-USR-ID followed by READNEXT (COUSR00C page forward). */
    List<SecurityUser> findByUserIdGreaterThanEqualOrderByUserIdAsc(String userId, Pageable pageable);

    /** Same browse when the record positioned on has already been displayed (PF8). */
    List<SecurityUser> findByUserIdGreaterThanOrderByUserIdAsc(String userId, Pageable pageable);

    /** READPREV before the first user of the current page (COUSR00C page backward, PF7). */
    List<SecurityUser> findByUserIdLessThanOrderByUserIdDesc(String userId, Pageable pageable);
}
