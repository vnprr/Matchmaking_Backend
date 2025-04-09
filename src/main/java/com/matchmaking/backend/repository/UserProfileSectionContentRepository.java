package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContent;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileSectionContentRepository extends JpaRepository<UserProfileSectionContent, Long> {
    List<UserProfileSectionContent> findByUserProfile(UserProfile userProfile);
    Optional<UserProfileSectionContent> findByUserProfileAndSectionDefinition(
            UserProfile userProfile,
            UserProfileSectionDefinition sectionDefinition);
    List<UserProfileSectionContent> findBySectionDefinition(UserProfileSectionDefinition sectionDefinition);
}