package com.matchmaking.backend.service.notification;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.notification.Notification;
import com.matchmaking.backend.model.notification.NotificationDTO;
import com.matchmaking.backend.model.notification.NotificationType;
import com.matchmaking.backend.model.recommendation.UserRecommendation;
import com.matchmaking.backend.repository.NotificationRepository;
import com.matchmaking.backend.repository.UserRecommendationRepository;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final UserRecommendationRepository userRecommendationRepository;

    @Transactional
    public Notification createNotification(User user, NotificationType type, String content, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .content(content)
                .read(false)
                .referenceId(referenceId)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String content, Long referenceId) {
        User user = userService.getUserById(userId);
        return createNotification(user, type, content, referenceId);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        User user = userService.getUserById(userId);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Powiadomienie o podanym ID nie istnieje"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationsCount(Long userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.countByUserAndReadFalse(user);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();

        dto.setId(notification.getId());
        dto.setContent(notification.getContent());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Felidae_Prionailurus_rubiginosus_phillipsi_3.jpg/1280px-Felidae_Prionailurus_rubiginosus_phillipsi_3.jpg");
        dto.setType(notification.getType());

        // Dodanie targetId bezpośrednio w NotificationService
        if (notification.getType().equals(NotificationType.NEW_RECOMMENDATION)) {
            try {
                UserRecommendation recommendation = userRecommendationRepository.findById(notification.getReferenceId())
                        .orElse(null);
                if (recommendation != null) {
                    User currentUser = userService.getCurrentUser();

                    // Przeniesiona logika z RecommendationService.getRecommendedUser
                    User recommendedUser;
                    if (recommendation.getFirstUser().getId().equals(currentUser.getId())) {
                        recommendedUser = recommendation.getSecondUser();
                    } else {
                        recommendedUser = recommendation.getFirstUser();
                    }

                    dto.setTargetId(recommendedUser.getProfile().getId());
                }
            } catch (Exception e) {
                // Obsługa błędu lub logowanie
            }
        }
        return dto;
    }


}