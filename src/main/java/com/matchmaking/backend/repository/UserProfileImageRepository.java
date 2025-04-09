package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.image.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {
    List<UserProfileImage> findByUserProfileOrderByDisplayOrderAsc(UserProfile userProfile);
    Optional<UserProfileImage> findByUserProfileAndIsMainTrue(UserProfile userProfile);
    int countByUserProfile(UserProfile userProfile);
}