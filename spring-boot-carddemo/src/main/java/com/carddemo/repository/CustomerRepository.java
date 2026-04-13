package com.carddemo.repository;

import com.carddemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCustLastName(String lastName);

    List<Customer> findByCustAddrStateCd(String stateCd);

    List<Customer> findByCustFicoCreditScoreGreaterThanEqual(Integer minScore);
}
