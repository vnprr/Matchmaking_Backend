package com.matchmaking.backend.service.user.profile.section;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContent;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinition;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.repository.UserProfileSectionContentRepository;
import com.matchmaking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileSectionInitializerService {

    private final UserProfileSectionContentRepository sectionContentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * Inicjalizuje sekcje profilu dla nowego użytkownika
     */
    @Transactional
    public void initializeProfileSections(UserProfile userProfile, List<UserProfileSectionDefinition> definitions) {
        List<UserProfileSectionContent> contents = new ArrayList<>();

        for (UserProfileSectionDefinition definition : definitions) {
            if (definition.isVisible()) {
                UserProfileSectionContent content = new UserProfileSectionContent();
                content.setUserProfile(userProfile);
                content.setSectionDefinition(definition);
                content.setContent(""); // Pusta zawartość
                contents.add(content);
            }
        }

        if (!contents.isEmpty()) {
            sectionContentRepository.saveAll(contents);
        }
    }

    /**
     * Inicjalizuje pustą zawartość nowej sekcji dla wszystkich istniejących profili
     */
    @Transactional
    public void initializeSectionForAllUsers(UserProfileSectionDefinition definition) {
        List<User> allUsers = userRepository.findAll();
        List<UserProfileSectionContent> contents = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getProfile() != null) {
                Optional<UserProfileSectionContent> existingContent =
                        sectionContentRepository.findByUserProfileAndSectionDefinition(user.getProfile(), definition);

                if (existingContent.isEmpty()) {
                    UserProfileSectionContent content = new UserProfileSectionContent();
                    content.setUserProfile(user.getProfile());
                    content.setSectionDefinition(definition);
                    content.setContent("");
                    contents.add(content);
                }
            }
        }

        if (!contents.isEmpty()) {
            sectionContentRepository.saveAll(contents);
        }
    }

    /**
     * Usuwa zawartość sekcji dla wszystkich profili użytkowników
     */
    @Transactional
    public void deleteSectionContentForAllUsers(UserProfileSectionDefinition definition) {
        List<UserProfileSectionContent> contents = sectionContentRepository.findBySectionDefinition(definition);
        if (!contents.isEmpty()) {
            sectionContentRepository.deleteAll(contents);
        }
    }

    private List<UserProfileSectionContent> findBySectionDefinition(UserProfileSectionDefinition definition) {
        // Implementacja zastępcza (powinno być dodane do repozytorium)
        List<UserProfile> allProfiles = userProfileRepository.findAll();
        List<UserProfileSectionContent> result = new ArrayList<>();

        for (UserProfile profile : allProfiles) {
            Optional<UserProfileSectionContent> content =
                    sectionContentRepository.findByUserProfileAndSectionDefinition(profile, definition);
            content.ifPresent(result::add);
        }

        return result;
    }
}