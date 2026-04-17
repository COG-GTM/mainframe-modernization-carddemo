package com.carddemo.menu.repository;

import com.carddemo.menu.entity.NavigationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for navigation context (session-based COMMAREA equivalent).
 */
@Repository
public interface NavigationContextRepository extends JpaRepository<NavigationContext, Long> {

    Optional<NavigationContext> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
