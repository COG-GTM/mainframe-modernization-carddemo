package com.carddemo.repository;

import com.carddemo.domain.User;

import java.util.List;
import java.util.Optional;

/** USRSEC (VSAM KSDS keyed on SEC-USR-ID). */
public interface UserRepository {

    Optional<User> findById(String userId);

    List<User> findAll();

    User save(User user);

    boolean deleteById(String userId);

    boolean existsById(String userId);
}
