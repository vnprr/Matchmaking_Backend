package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.ProfileResponseDTO;
import com.matchmaking.backend.model.ProfileUpdateRequestDTO;
import com.matchmaking.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(Principal principal) {
        return ResponseEntity.ok(profileService.getUserProfile(principal.getName()));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Principal principal,
            @Valid @RequestBody ProfileUpdateRequestDTO request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        profileService.updateUserProfile(principal.getName(), request);
        return ResponseEntity.ok("Profile updated");
    }
}