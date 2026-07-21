package com.carddemo.billpay.repository;

import com.carddemo.billpay.domain.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** ACCTDAT KSDS read-for-update / rewrite (COBIL00C: READ-ACCTDAT-FILE, UPDATE-ACCTDAT-FILE). */
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
