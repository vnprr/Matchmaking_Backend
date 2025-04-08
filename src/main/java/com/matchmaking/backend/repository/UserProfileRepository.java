package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
