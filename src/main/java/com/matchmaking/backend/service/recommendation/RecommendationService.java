package com.matchmaking.backend.service.recommendation;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.notification.NotificationType;
import com.matchmaking.backend.model.recommendation.RecommendationStatus;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import com.matchmaking.backend.model.recommendation.UserRecommendationDTO;
import com.matchmaking.backend.repository.NotificationRepository;
import com.matchmaking.backend.repository.UserRecommendationRepository;
import com.matchmaking.backend.service.notification.NotificationService;
import com.matchmaking.backend.service.UserService;
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
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public UserRecommendation createRecommendation(Long firstUserId, Long secondUserId) {
        User firstUser = userService.getUserById(firstUserId);
        User secondUser = userService.getUserById(secondUserId);

        // Sprawdź, czy rekomendacja już istnieje
        Optional<UserRecommendation> existingRecommendation =
                recommendationRepository.findByUsers(firstUser, secondUser);

        if (existingRecommendation.isPresent()) {
            return existingRecommendation.get();
        }

        // Utwórz nową rekomendację
        UserRecommendation recommendation = UserRecommendation.builder()
                .firstUser(firstUser)
                .secondUser(secondUser)
                .status(RecommendationStatus.NEW)
                .createdBy(userService.getCurrentUser())
                .build();

        recommendation = recommendationRepository.save(recommendation);

        // Utwórz powiadomienia dla obu użytkowników
        notificationService.createNotification(
                firstUser,
                NotificationType.NEW_RECOMMENDATION,
                "Administrator polecił Ci nowego użytkownika: " + secondUser.getProfile().getFirstName(),
                recommendation.getId()
        );

        notificationService.createNotification(
                secondUser,
                NotificationType.NEW_RECOMMENDATION,
                "Administrator polecił Ci nowego użytkownika: " + firstUser.getProfile().getFirstName(),
                recommendation.getId()
        );

        return recommendation;
    }

    @Transactional(readOnly = true)
    public Page<UserRecommendationDTO> getUserRecommendations(
            Long userId,
            int page,
            int size
    ) {
        User user = userService.getUserById(userId);
        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<UserRecommendation> recommendations = recommendationRepository.findAllByUser(user, pageable);

        return recommendations.map(recommendation -> mapToDto(recommendation, userId));
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

    private UserRecommendationDTO mapToDto(UserRecommendation recommendation, Long userId) {
        UserRecommendationDTO dto = new UserRecommendationDTO();

        // Mapowanie podstawowych pól
        dto.setId(recommendation.getId());
        dto.setStatus(recommendation.getStatus());
        dto.setCreatedAt(recommendation.getCreatedAt());
        dto.setViewedAt(recommendation.getViewedAt());

        dto.setCreatedById(recommendation.getCreatedBy().getId());

        // Określenie polecanego użytkownika
        User recommendedUser;
        if (recommendation.getFirstUser().getId().equals(userId)) {
            recommendedUser = recommendation.getSecondUser();
        } else {
            recommendedUser = recommendation.getFirstUser();
        }

        // Ustawienie pól związanych z polecanym użytkownikiem
        dto.setRecommendedUserId(recommendedUser.getId());
        dto.setRecommendedUserEmail(recommendedUser.getEmail());

        return dto;
    }

    public void deleteRecommendation(Long recommendationId) {
        recommendationRepository.deleteById(recommendationId);
    }

//    @Transactional(readOnly = true)
//    public UserProfile getRecommendedUserProfile(Long recommendationId) {
//        UserRecommendation recommendation = recommendationRepository.findById(recommendationId)
//                .orElseThrow(() -> new IllegalArgumentException("Rekomendacja nie istnieje"));
//
//        User currentUser = userService.getCurrentUser();
//        User recommendedUser = getRecommendedUser(recommendation, currentUser.getId());
//
//        return recommendedUser.getProfile();
//    }
//
    public User getRecommendedUser (UserRecommendation recommendation, Long userId) {

        if (recommendation.getFirstUser().getId().equals(userId)) {
            return recommendation.getSecondUser();
        } else {
            return recommendation.getFirstUser();
        }
    }
}