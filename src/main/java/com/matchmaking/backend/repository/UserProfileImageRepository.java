package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.model.image.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {
    List<UserProfileImage> findByUserProfileOrderByDisplayOrderAsc(UserProfile userProfile);
    Optional<UserProfileImage> findByUserProfileAndIsAvatarTrue(UserProfile userProfile);
    @Query("SELECT MAX(i.displayOrder) FROM UserProfileImage i WHERE i.userProfile = ?1")
    Integer findMaxDisplayOrder(UserProfile userProfile);


    List<UserProfileImage> findByUserProfile(UserProfile userProfile);
}
