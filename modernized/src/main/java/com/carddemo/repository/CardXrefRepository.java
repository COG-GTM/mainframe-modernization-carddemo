package com.carddemo.repository;

import com.carddemo.domain.CardXref;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    /** Equivalent of the CXACAIX alternate index path (account id -> xref record). */
    Optional<CardXref> findFirstByAccountIdOrderByCardNumber(Long accountId);

    List<CardXref> findByAccountIdOrderByCardNumber(Long accountId);

    List<CardXref> findByCustomerIdOrderByCardNumber(Long customerId);
}
