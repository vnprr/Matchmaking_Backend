package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {

    @Query("SELECT r FROM UserRecommendation r WHERE (r.firstProfile = :profile OR r.secondProfile = :profile)")
    Page<UserRecommendation> findAllByProfile(UserProfile profile, Pageable pageable);

    @Query("SELECT r FROM UserRecommendation r WHERE " +
            "(r.firstProfile = :firstProfile AND r.secondProfile = :secondProfile) OR " +
            "(r.firstProfile = :secondProfile AND r.secondProfile = :firstProfile)")
    Optional<UserRecommendation> findByProfiles(UserProfile firstProfile, UserProfile secondProfile);
}
