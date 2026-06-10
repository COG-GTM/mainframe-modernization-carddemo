package com.carddemo.repository;

import com.carddemo.entity.TransactionReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRejectRepository extends JpaRepository<TransactionReject, Long> {
}
