


package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.section.UserProfileSectionContentChangeDTO;
import com.matchmaking.backend.model.section.UserProfileSectionContentRequestDTO;
import com.matchmaking.backend.service.section.UserProfileSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile/sections")
@RequiredArgsConstructor
public class UserProfileSectionController {

    private final UserProfileSectionService sectionService;

    /**
     * Pobierz sekcje profilu obecnego użytkownika
     * @return Lista sekcji profilu użytkownika
     */
    @GetMapping("/me")
    public ResponseEntity<List<UserProfileSectionContentRequestDTO>> getCurrentUserProfileSections() {
        return ResponseEntity.ok(sectionService.getUserProfileSections());
    }

    /**
     * Zaktualizuj sekcję profilu użytkownika po ID sekcji
     * @param sectionId ID sekcji @param content
     * @return Lista sekcji profilu użytkownika
     */
    @PutMapping("/me/{sectionId}")
    public ResponseEntity<?> updateCurrentUserProfileSection(
            @PathVariable Long sectionId,
            @RequestBody UserProfileSectionContentChangeDTO sectionContentChange) {
        try {
            sectionService.updateUserProfileSection(sectionId, sectionContentChange);
            return ResponseEntity.ok("Sekcja została zaktualizowana");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Pobierz sekcje profilu użytkownika po ID profilu
     * @param userProfileId ID profilu użytkownika
     * @return Lista sekcji profilu użytkownika
     */
    @GetMapping("/{userProfileId}")
    public ResponseEntity<List<UserProfileSectionContentRequestDTO>> getUserProfileSections(
            @PathVariable Long userProfileId
    ) {
        return ResponseEntity.ok(sectionService.getUserProfileSections(userProfileId));
    }
}
