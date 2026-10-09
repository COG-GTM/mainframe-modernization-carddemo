package com.carddemo.repository;

import com.carddemo.domain.Transaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findFirstByOrderByIdDesc();

    Page<Transaction> findAllByOrderByIdAsc(Pageable pageable);

    List<Transaction> findByCardNumberOrderByIdAsc(String cardNumber);

    List<Transaction> findByProcessingTimestampGreaterThanEqualAndProcessingTimestampLessThanOrderByCardNumberAscIdAsc(
            String from, String toExclusive);
}
