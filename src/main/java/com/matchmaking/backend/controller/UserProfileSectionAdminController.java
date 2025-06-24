package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.section.UserProfileSectionDefinitionChangeDTO;
import com.matchmaking.backend.model.section.UserProfileSectionDefinitionDTO;
import com.matchmaking.backend.service.section.UserProfileSectionAdminService;
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
     * @param id ID sekcji
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileSectionDefinitionDTO> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionAdminService.getSectionDefinitionById(id));
    }

    /**
     * Tworzy nową sekcję profilu (dodawaną zawsze na koniec listy)
     * @param sectionDTO Dane sekcji do utworzenia
     */
    @PostMapping
    public ResponseEntity<UserProfileSectionDefinitionDTO> createSection(
            @RequestBody UserProfileSectionDefinitionChangeDTO sectionDTO) {
        return ResponseEntity.ok(sectionAdminService.createSectionDefinition(sectionDTO));
    }

    /**
     * Aktualizuje sekcję profilu (bez zmiany kolejności)
     * @param id ID sekcji do aktualizacji
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileSectionDefinitionDTO> updateSection(
            @PathVariable Long id,
            @RequestBody UserProfileSectionDefinitionChangeDTO sectionDTO) {
        return ResponseEntity.ok(sectionAdminService.updateSectionDefinition(id, sectionDTO));
    }

    /**
     * Usuwa sekcję profilu i przenumerowuje pozostałe elementy
     * @param id ID sekcji do usunięcia
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionAdminService.deleteSectionDefinition(id);
        return ResponseEntity.noContent().build();
    }

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