package com.carddemo.acctupdate.repository;

import com.carddemo.acctupdate.domain.AccountRecord;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<AccountRecord, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountRecord a where a.acctId = :id")
    Optional<AccountRecord> findByIdForUpdate(Long id);
}
