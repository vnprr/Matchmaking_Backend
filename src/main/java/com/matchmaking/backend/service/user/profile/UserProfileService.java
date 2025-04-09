package com.matchmaking.backend.service.user.profile;

import com.matchmaking.backend.model.*;
import com.matchmaking.backend.service.UserService;
import com.matchmaking.backend.service.user.profile.section.UserProfileSectionInitializerService;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinition;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.repository.UserProfileSectionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileSectionInitializerService sectionInitializerService;
    private final UserProfileSectionDefinitionRepository sectionDefinitionRepository;

    @Transactional
    public void initializeProfileSections(UserProfile userProfile) {
        List<UserProfileSectionDefinition> definitions =
                sectionDefinitionRepository.findAllByVisibleTrueOrderByDisplayOrderAsc();
        sectionInitializerService.initializeProfileSections(userProfile, definitions);
    }

    public UserProfileRequestDTO getCurrentUserProfile() {
        // Pobierz zalogowanego użytkownika
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        UserProfile profile = user.getProfile();

        // Konwersja do DTO
        UserProfileRequestDTO dto = new UserProfileRequestDTO();
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setGender(profile.getGender());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setBio(profile.getBio());

        return dto;
    }

//    @Transactional
//    public UserProfile createUserProfile(User user, String firstName, String lastName) {
//        userProfileCreatorService.createUserProfile(
//                user,
//                firstName,
//                lastName
//        );
//
//        UserProfile userProfile = new UserProfile();
//        userProfile.setUser(user);
//        userProfile.setFirstName("");
//        userProfile.setLastName("");
//        userProfile.setDateOfBirth(null);
//        userProfile.setBio("");
//
//        // Inicjalizacja sekcji profilu
//        initializeProfileSections(userProfile);
//
//        // Zapisz profil użytkownika
//        userProfileRepository.save(userProfile);
//
//        return userProfile;
//    }

    @Transactional
    public ResponseEntity<?> updateUserProfile(UserProfileRequestDTO profileDTO) {
        // Pobierz zalogowanego użytkownika
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        UserProfile profile = user.getProfile();

        // Aktualizacja danych profilu
        profile.setFirstName(profileDTO.getFirstName());
        profile.setLastName(profileDTO.getLastName());
        profile.setGender(profileDTO.getGender());
        profile.setDateOfBirth(profileDTO.getDateOfBirth());
        profile.setBio(profileDTO.getBio());

        userProfileRepository.save(profile);

        return ResponseEntity.ok("Profil został zaktualizowany");
    }
}