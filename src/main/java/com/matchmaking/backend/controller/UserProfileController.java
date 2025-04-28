package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.UserProfileDTO;
import com.matchmaking.backend.service.IdEncryptionService;
import com.matchmaking.backend.service.user.profile.UserProfileService;
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
     @param userId ID użytkownika
     @return DTO profilu użytkownika
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        // Long userId = idEncryptionService.decryptId(publicId);
        return ResponseEntity.ok(userProfileService.getUserProfileById(userId));
    }

    /**
     Zaktualizuj profil obecnego użytkownika
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUserProfile(@RequestBody UserProfileDTO profileDTO) {
        return userProfileService.updateUserProfile(profileDTO);
    }

    /**
     Zaktualizuj profil użytkownika po ID
     @param userId ID użytkownika
     @return ResponseEntity z komunikatem o powodzeniu
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDTO profileDTO) {
       return userProfileService.updateUserProfile(profileDTO, userId);
    }
}