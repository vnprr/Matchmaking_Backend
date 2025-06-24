package com.matchmaking.backend.controller;


import com.matchmaking.backend.model.recommendation.RecommendationStatus;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import com.matchmaking.backend.model.recommendation.UserRecommendationDTO;
import com.matchmaking.backend.service.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Tworzy rekomendację między dwoma profilami użytkowników.
     * Dostępne tylko dla administratorów.
     *
     * @param firstProfileId  ID pierwszego profilu użytkownika
     * @param secondProfileId ID drugiego profilu użytkownika
     * @return Utworzona rekomendacja
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserRecommendation> createRecommendation(
            @RequestParam Long firstProfileId,
            @RequestParam Long secondProfileId) {
        return ResponseEntity.ok(recommendationService.createRecommendation(firstProfileId, secondProfileId));
    }

    /**
     * Pobiera rekomendacje dla danego profilu użytkownika.
     * @param profileId ID profilu użytkownika
     * @param page Numer strony (domyślnie 0)
     * @param size Rozmiar strony (domyślnie 20)
     * @return Lista rekomendacji dla danego profilu
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<Page<UserRecommendationDTO>> getUserRecommendations(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ) {
        return ResponseEntity
                .ok(recommendationService
                .getUserRecommendations(
                    profileId,
                    page,
                    size
                ));
    }

    /**
     * Zaktualizuje status rekomendacji.
     * @param recommendationId ID rekomendacji
     * @param status Nowy status rekomendacji
     */
    @PutMapping("/{recommendationId}")
    public ResponseEntity<UserRecommendation> updateRecommendationStatus(
            @PathVariable Long recommendationId,
            @RequestParam RecommendationStatus status) {
        return ResponseEntity.ok(recommendationService.updateRecommendationStatus(recommendationId, status));
    }

    /**
     * Pobiera rekomendację po ID.
     * @param recommendationId ID rekomendacji
     * @return Rekomendacja
     */
    @DeleteMapping("/{recommendationId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long recommendationId) {
        recommendationService.deleteRecommendation(recommendationId);
        return ResponseEntity.noContent().build();
    }
}
