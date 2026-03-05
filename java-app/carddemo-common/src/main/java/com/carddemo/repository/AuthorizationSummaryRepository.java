package com.carddemo.repository;

import com.carddemo.entity.AuthorizationSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuthorizationSummary entity.
 * Replaces IMS DL/I GU/GN calls on authorization segments.
 */
@Repository
public interface AuthorizationSummaryRepository extends JpaRepository<AuthorizationSummary, Long> {
    Page<AuthorizationSummary> findByCardNum(String cardNum, Pageable pageable);
    List<AuthorizationSummary> findByAuthTimestampBefore(LocalDateTime threshold);
}
