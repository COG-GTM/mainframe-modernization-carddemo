package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.ReportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for report request tracking.
 *
 * COBOL Traceability: Replaces the CICS Transient Data queue (WRITEQ TD)
 * used by CORPT00C to submit report generation requests.
 */
@Repository
public interface ReportRequestRepository extends JpaRepository<ReportRequestEntity, Long> {
}
