package com.carddemo.repository;

import com.carddemo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Page<User> findByUserIdStartingWith(String prefix, Pageable pageable);
}
