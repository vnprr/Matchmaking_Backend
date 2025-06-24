package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.profile.UserProfileContextDTO;
import com.matchmaking.backend.service.profile.UserProfileContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/context")
@RequiredArgsConstructor
public class UserProfileContextController {

    private final UserProfileContextService userProfileContextService;

    /**
     * Pobiera kontekst dostępu do profilu użytkownika.
     *
     * @param userId ID użytkownika, którego profil jest sprawdzany
     * @return DTO z informacjami o uprawnieniach
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileContextDTO> getProfileContext(@PathVariable Long userId) {
        return ResponseEntity.ok(userProfileContextService.getProfileContext(userId));
    }

    /**
     * Pobiera kontekst profilu dla aktualnie zalogowanego użytkownika.
     *
     * @return DTO z informacjami o uprawnieniach do profilu aktualnego użytkownika
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileContextDTO> getCurrentUserProfileContext() {
        return ResponseEntity.ok(userProfileContextService.getCurrentUserProfileContext());
    }
}
