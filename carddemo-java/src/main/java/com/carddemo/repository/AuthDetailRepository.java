package com.carddemo.repository;

import com.carddemo.entity.AuthDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuthDetailRepository extends JpaRepository<AuthDetail, Long> {
    List<AuthDetail> findByAuthSummaryId(Long summaryId);
}
