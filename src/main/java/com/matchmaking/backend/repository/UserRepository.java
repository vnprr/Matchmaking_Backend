package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);


    Page<User> findByEmailContainingOrProfileFirstNameContainingOrProfileLastNameContaining(
            String email, String firstName, String lastName, Pageable pageable);
}