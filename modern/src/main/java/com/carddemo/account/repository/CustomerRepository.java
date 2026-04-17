package com.carddemo.account.repository;

import com.carddemo.account.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CUSTDAT (Customer Master) access.
 * Migrated from: EXEC CICS READ FILE('CUSTDAT') in COACTVWC.cbl / COACTUPC.cbl
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
