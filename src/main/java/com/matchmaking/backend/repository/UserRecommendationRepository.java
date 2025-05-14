package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {

    @Query("SELECT r FROM UserRecommendation r WHERE (r.firstUser = :user OR r.secondUser = :user)")
    Page<UserRecommendation> findAllByUser(User user, Pageable pageable);

    @Query("SELECT r FROM UserRecommendation r WHERE " +
            "(r.firstUser = :firstUser AND r.secondUser = :secondUser) OR " +
            "(r.firstUser = :secondUser AND r.secondUser = :firstUser)")
    Optional<UserRecommendation> findByUsers(User firstUser, User secondUser);
}