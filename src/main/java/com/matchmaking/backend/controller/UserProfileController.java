package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.profile.UserProfileDTO;
import com.matchmaking.backend.service.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    // private final IdEncryptionService idEncryptionService;

    /**
     Pobierz profil obecnego użytkownika
     @return DTO profilu użytkownika
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    /**
     Pobierz profil użytkownika po ID
     @param userProfileId ID użytkownika
     @return DTO profilu użytkownika
     */
    @GetMapping("/{userProfileId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userProfileId) {
        // Long userId = idEncryptionService.decryptId(publicId);
        return ResponseEntity.ok(userProfileService.getProfileById(userProfileId));
    }

    /**
     Zaktualizuj profil obecnego użytkownika
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UserProfileDTO profileDTO) {
        return userProfileService.updateCurrentUserProfile(profileDTO);
    }

    /**
     Zaktualizuj profil użytkownika po ID
     @param userProfileId ID użytkownika
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @PutMapping("/{userProfileId}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userProfileId,
            @RequestBody UserProfileDTO profileDTO) {
       return userProfileService.updateUserProfile(profileDTO, userProfileId);
    }
}