package com.carddemo.repository;

import com.carddemo.entity.AuthorizationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for AuthorizationDetail entity.
 * Replaces IMS DL/I GN calls on authorization detail segment.
 */
@Repository
public interface AuthorizationDetailRepository extends JpaRepository<AuthorizationDetail, Long> {
}
