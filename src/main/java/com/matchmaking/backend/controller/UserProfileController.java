package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.UserProfileRequestDTO;
import com.matchmaking.backend.service.user.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileRequestDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    @PutMapping
    public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileRequestDTO profileDTO) {
        return userProfileService.updateUserProfile(profileDTO);
    }
}