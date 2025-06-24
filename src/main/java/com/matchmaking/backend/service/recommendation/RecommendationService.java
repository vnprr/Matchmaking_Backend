package com.matchmaking.backend.service.recommendation;

import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.model.notification.NotificationType;
import com.matchmaking.backend.model.recommendation.RecommendationStatus;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import com.matchmaking.backend.model.recommendation.UserRecommendationDTO;
import com.matchmaking.backend.repository.NotificationRepository;
import com.matchmaking.backend.repository.UserRecommendationRepository;
import com.matchmaking.backend.service.notification.NotificationService;
import com.matchmaking.backend.service.UserService;
import com.matchmaking.backend.service.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRecommendationRepository recommendationRepository;
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public UserRecommendation createRecommendation(Long firstProfileId, Long secondProfileId) {
        UserProfile firstProfile = userProfileService.getProfileEntityById(firstProfileId);
        UserProfile secondProfile = userProfileService.getProfileEntityById(secondProfileId);

        // Sprawdź, czy rekomendacja już istnieje
        Optional<UserRecommendation> existingRecommendation =
                recommendationRepository.findByProfiles(firstProfile, secondProfile);

        if (existingRecommendation.isPresent()) {
            return existingRecommendation.get();
        }

        // Utwórz nową rekomendację
        UserRecommendation recommendation = UserRecommendation.builder()
                .firstProfile(firstProfile)
                .secondProfile(secondProfile)
                .status(RecommendationStatus.NEW)
                .createdBy(userService.getCurrentUser())
                .build();

        recommendation = recommendationRepository.save(recommendation);

        // Utwórz powiadomienia dla obu użytkowników
        notificationService.createNotification(
                firstProfile.getUser(),
                NotificationType.NEW_RECOMMENDATION,
                "Administrator polecił Ci nowy profil: " + secondProfile.getFirstName(),
                recommendation.getId()
        );

        notificationService.createNotification(
                secondProfile.getUser(),
                NotificationType.NEW_RECOMMENDATION,
                "Administrator polecił Ci nowy profil: " + firstProfile.getFirstName(),
                recommendation.getId()
        );

        return recommendation;
    }

    @Transactional(readOnly = true)
    public Page<UserRecommendationDTO> getUserRecommendations(
            Long profileId,
            int page,
            int size
    ) {
        UserProfile profile = userProfileService.getProfileEntityById(profileId);
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<UserRecommendation> recommendations = recommendationRepository.findAllByProfile(profile, pageable);

        return recommendations.map(recommendation -> mapToDto(recommendation, profileId));
    }

    @Transactional
    public UserRecommendation updateRecommendationStatus(Long recommendationId, RecommendationStatus status) {
        UserRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Rekomendacja o podanym ID nie istnieje"));

        recommendation.setStatus(status);

        if (status == RecommendationStatus.VIEWED && recommendation.getViewedAt() == null) {
            recommendation.setViewedAt(LocalDateTime.now());
        }

        return recommendationRepository.save(recommendation);
    }

    private UserRecommendationDTO mapToDto(UserRecommendation recommendation, Long profileId) {
        UserRecommendationDTO dto = new UserRecommendationDTO();

        // Mapowanie podstawowych pól
        dto.setId(recommendation.getId());
        dto.setStatus(recommendation.getStatus());
        dto.setCreatedAt(recommendation.getCreatedAt());
        dto.setViewedAt(recommendation.getViewedAt());

        dto.setCreatedById(recommendation.getCreatedBy().getId());

        // Określenie polecanego profilu
        UserProfile recommendedProfile;
        if (recommendation.getFirstProfile().getId().equals(profileId)) {
            recommendedProfile = recommendation.getSecondProfile();
        } else {
            recommendedProfile = recommendation.getFirstProfile();
        }

        // Ustawienie pól związanych z polecanym profilem
        dto.setRecommendedProfileId(recommendedProfile.getId());
        dto.setRecommendedProfileName(recommendedProfile.getFirstName() + " " + recommendedProfile.getLastName());

        return dto;
    }

    public void deleteRecommendation(
            Long recommendationId
    ) {
        recommendationRepository.deleteById(recommendationId);
    }

    public UserProfile getRecommendedProfile(UserRecommendation recommendation, Long profileId) {
        if (recommendation.getFirstProfile().getId().equals(profileId)) {
            return recommendation.getSecondProfile();
        } else {
            return recommendation.getFirstProfile();
        }
    }
}
