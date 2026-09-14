package com.carddemo.acctupdate.repository;

import com.carddemo.acctupdate.domain.CustomerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import javax.persistence.LockModeType;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerRecord, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerRecord c where c.custId = :id")
    Optional<CustomerRecord> findByIdForUpdate(Long id);
}
