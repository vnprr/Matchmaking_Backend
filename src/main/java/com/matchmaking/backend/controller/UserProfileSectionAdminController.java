package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinitionChangeDTO;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinitionDTO;
import com.matchmaking.backend.service.user.profile.section.UserProfileSectionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/profile-sections")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserProfileSectionAdminController {

    private final UserProfileSectionAdminService sectionAdminService;

    /**
     * Pobiera wszystkie sekcje profilu
     */
    @GetMapping
    public ResponseEntity<List<UserProfileSectionDefinitionDTO>> getAllSections() {
        return ResponseEntity.ok(sectionAdminService.getAllSectionDefinitions());
    }

    /**
     * Pobiera sekcję po ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileSectionDefinitionDTO> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionAdminService.getSectionDefinitionById(id));
    }

    /**
     * Tworzy nową sekcję profilu (dodawaną zawsze na koniec listy)
     */
    @PostMapping
    public ResponseEntity<UserProfileSectionDefinitionDTO> createSection(
            @RequestBody UserProfileSectionDefinitionChangeDTO sectionDTO) {
        return ResponseEntity.ok(sectionAdminService.createSectionDefinition(sectionDTO));
    }

    /**
     * Aktualizuje sekcję profilu (bez zmiany kolejności)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileSectionDefinitionDTO> updateSection(
            @PathVariable Long id,
            @RequestBody UserProfileSectionDefinitionChangeDTO sectionDTO) {
        return ResponseEntity.ok(sectionAdminService.updateSectionDefinition(id, sectionDTO));
    }

    /**
     * Usuwa sekcję profilu i przenumerowuje pozostałe elementy
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionAdminService.deleteSectionDefinition(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aktualizuje kolejność wyświetlania sekcji z zachowaniem ciągłej numeracji 0-n
     */
//    @PutMapping("/order")
//    public ResponseEntity<Void> updateSectionsOrder(
//            @RequestBody List<UserProfileSectionDefinitionDTO> sections) {
//        sectionAdminService.updateSectionsOrder(sections);
//        return ResponseEntity.ok().build();
//    }

    /**
     * Przesuwa sekcję o jedną pozycję w górę
     */
    @PutMapping("/{id}/move-up")
    public ResponseEntity<UserProfileSectionDefinitionDTO> moveSectionUp(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sectionAdminService.moveUp(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Przesuwa sekcję o jedną pozycję w dół
     */
    @PutMapping("/{id}/move-down")
    public ResponseEntity<UserProfileSectionDefinitionDTO> moveSectionDown(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(sectionAdminService.moveDown(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}