package com.matchmaking.backend.service.profile;

import com.matchmaking.backend.model.auth.User;
import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.model.section.UserProfileSectionDefinition;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.repository.UserProfileSectionDefinitionRepository;
import com.matchmaking.backend.service.section.UserProfileSectionInitializerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileCreatorService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileSectionInitializerService sectionInitializerService;
    private final UserProfileSectionDefinitionRepository sectionDefinitionRepository;

    @Transactional
    public UserProfile createUserProfile(User user, String firstName, String lastName) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setFirstName(firstName != null ? firstName : "");
        userProfile.setLastName(lastName != null ? lastName : "");
        userProfile.setDateOfBirth(null);
        userProfile.setBio("");

        userProfile = userProfileRepository.save(userProfile);

        // Inicjalizacja sekcji profilu
        initializeProfileSections(userProfile);

        return userProfile;
    }

    @Transactional
    public void initializeProfileSections(UserProfile userProfile) {
        List<UserProfileSectionDefinition> definitions =
                sectionDefinitionRepository.findAllByVisibleTrueOrderByDisplayOrderAsc();
        sectionInitializerService.initializeProfileSections(userProfile, definitions);
    }
}