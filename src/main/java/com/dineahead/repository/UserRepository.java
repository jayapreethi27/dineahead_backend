package com.dineahead.repository;

import com.dineahead.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {


    // ================================
    // ADDED FOR AUTHENTICATION
    // ================================
    Optional<User> findByEmail(
            String email
    );


    // ================================
    // ADDED FOR REGISTRATION VALIDATION
    // ================================
    boolean existsByEmail(
            String email
    );

}