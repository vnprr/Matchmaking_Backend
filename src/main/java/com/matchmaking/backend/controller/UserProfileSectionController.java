


package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentChangeDTO;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentRequestDTO;
import com.matchmaking.backend.service.user.profile.section.UserProfileSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileSectionController {

    private final UserProfileSectionService sectionService;

    /**
     * Pobierz sekcje profilu obecnego użytkownika
     * @return Lista sekcji profilu użytkownika
     */
    @GetMapping("/me/sections")
    public ResponseEntity<List<UserProfileSectionContentRequestDTO>> getCurrentUserProfileSections() {
        return ResponseEntity.ok(sectionService.getUserProfileSections());
    }

    /**
     * Zaktualizuj sekcję profilu użytkownika po ID sekcji
     * @param sectionId ID sekcji @param content
     * @return Lista sekcji profilu użytkownika
     */
    @PutMapping("/me/sections/{sectionId}")
    public ResponseEntity<?> updateCurrentUserProfileSection(
            @PathVariable Long sectionId,
            @RequestBody String content) {
        try {
            sectionService.updateUserProfileSection(sectionId, content);
            return ResponseEntity.ok("Sekcja została zaktualizowana");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PutMapping("/me/sections")
//    public ResponseEntity<?> updateCurrentUserProfileSections(
//            @RequestBody List<UserProfileSectionContentChangeDTO> sections
//    ) {
//        try {
//            sectionService.updateUserProfileSections(sections);
//            return ResponseEntity.ok("Sekcje zostały zaktualizowane");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    // Po ID użytkownika:
    @GetMapping("/{userId}/sections")
    public ResponseEntity<List<UserProfileSectionContentRequestDTO>> getUserProfileSections(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(sectionService.getUserProfileSections(userId));
    }

//    @PutMapping("/{userId}sections/{sectionId}")
//    public ResponseEntity<?> updateUserProfileSection(
//            @PathVariable Long userId,
//            @PathVariable Long sectionId,
//            @RequestBody String content) {
//        try {
//            sectionService.updateUserProfileSection(sectionId, content);
//            return ResponseEntity.ok("Sekcja została zaktualizowana");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

//    @PutMapping("/me")
//    public ResponseEntity<?> updateUserProfileSections(
//            @RequestBody List<UserProfileSectionContentChangeDTO> sections
//    ) {
//        try {
//            sectionService.updateUserProfileSections(sections);
//            return ResponseEntity.ok("Sekcje zostały zaktualizowane");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}


//package com.matchmaking.backend.controller;
//
//import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentChangeDTO;
//import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentRequestDTO;
//import com.matchmaking.backend.service.user.profile.section.UserProfileSectionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/profile/sections")
//@RequiredArgsConstructor
//public class UserProfileSectionController {
//
//    private final UserProfileSectionService sectionService;
//
//    @GetMapping
//    public ResponseEntity<List<UserProfileSectionContentRequestDTO>> getUserProfileSections() {
//        return ResponseEntity.ok(sectionService.getUserProfileSections());
//    }
//
//    @PutMapping("/{sectionId}")
//    public ResponseEntity<?> updateUserProfileSection(
//            @PathVariable Long sectionId,
//            @RequestBody String content) {
//        try {
//            sectionService.updateUserProfileSection(sectionId, content);
//            return ResponseEntity.ok("Sekcja została zaktualizowana");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PutMapping
//    public ResponseEntity<?> updateUserProfileSections(
//            @RequestBody List<UserProfileSectionContentChangeDTO> sections
//    ) {
//        try {
//            sectionService.updateUserProfileSections(sections);
//            return ResponseEntity.ok("Sekcje zostały zaktualizowane");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//}