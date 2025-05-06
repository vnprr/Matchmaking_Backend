package com.matchmaking.backend.service.user.profile;

import com.matchmaking.backend.model.*;
import com.matchmaking.backend.model.user.profile.UserProfileContextDTO;
import com.matchmaking.backend.service.UserService;
import com.matchmaking.backend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserService userService;
    private final UserProfileContextService userProfileContextService;
    private final UserProfileRepository userProfileRepository;

    /**
     Pobiera profil zalogowanego użytkownika
     @return Profil użytkownika
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        return mapToDTO(user.getProfile());
    }

    /**
     Pobiera profil użytkownika po ID profilu
     @param profileId ID profilu użytkownika
     @return Profil użytkownika
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getProfileById(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));
        return mapToDTO(profile);
    }

    /**
     Zaktualizuj profil
     @param profileDTO Zaktualizowany profil
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @Transactional
    public ResponseEntity<?> updateCurrentUserProfile(UserProfileDTO profileDTO) {
        UserProfile profile = getAuthenticatedUserProfile();
        updateProfileFields(profile, profileDTO);
        userProfileRepository.save(profile);
        return ResponseEntity.ok("Profil został zaktualizowany");
    }

    /**
     Zaktualizuj profil, jeśli użytkownik ma uprawnienia do edycji
     @param profileDTO Zaktualizowany profil
     @param profileId ID użytkownika
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @Transactional
    public ResponseEntity<?> updateUserProfile(UserProfileDTO profileDTO, Long profileId) {
        if (!userProfileContextService.canEdit(profileId)) {
            return ResponseEntity.status(403).body("Brak uprawnień do edycji tego profilu");
        }

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));

        updateProfileFields(profile, profileDTO);
        userProfileRepository.save(profile);
        return ResponseEntity.ok("Profil został zaktualizowany");
    }

    /**
     Zaktualizuj profil użytkownika przez administratora
     @param profileDTO Zaktualizowany profil
     @return ResponseEntity z komunikatem o powodzeniu
     @throws RuntimeException Jeśli profil użytkownika nie został znaleziony
     */
    @Transactional
    public ResponseEntity<?> updateUserProfileByAdmin(UserProfileDTO profileDTO, Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono profilu użytkownika"));
        updateProfileFields(profile, profileDTO);
        userProfileRepository.save(profile);
        return ResponseEntity.ok("Profil został zaktualizowany przez administratora");
    }

    /**
     Zwraca profil użytkownika na podstawie kontekstu bezpieczeństwa
     @return Profil użytkownika
     */
    private UserProfile getAuthenticatedUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        return user.getProfile();
    }

    private UserProfileDTO mapToDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        //dto.setUserId(profile.getUser().getId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setGender(profile.getGender());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setBio(profile.getBio());

        // Dodaj URL zdjęcia profilowego, jeśli istnieje
//        Optional<UserProfileImage> mainImage = imageRepository.findByUserProfileAndIsMainTrue(profile);
//        mainImage.ifPresent(image ->
//                dto.setProfileImageUrl(image.getProfileImageUrl() != null ?
//                        image.getProfileImageUrl() : image.getImageUrl())
//        );
        return dto;
    }

    /**
     Uaktualnia pola profilu na podstawie danych z DTO
     @param profile Profil użytkownika
     @param dto Zaktualizowane dane profilu
     */
    private void updateProfileFields(UserProfile profile, UserProfileDTO dto) {
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setGender(dto.getGender());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setBio(dto.getBio());
    }
}