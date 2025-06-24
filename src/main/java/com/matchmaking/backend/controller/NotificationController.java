package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.notification.Notification;
import com.matchmaking.backend.model.notification.NotificationDTO;
import com.matchmaking.backend.service.notification.NotificationService;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Pobiera powiadomienia dla zalogowanego użytkownika.
     *
     * @param pageable Parametry paginacji.
     * @return Lista powiadomień użytkownika.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getCurrentUserNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    /**
     * Pobiera liczbę nieprzeczytanych powiadomień dla zalogowanego użytkownika.
     *
     * @return Liczba nieprzeczytanych powiadomień.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationsCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        return ResponseEntity.ok(notificationService.getUnreadNotificationsCount(userId));
    }

    /**
     * Oznacza powiadomienie jako przeczytane.
     *
     * @param notificationId ID powiadomienia do oznaczenia jako przeczytane.
     * @return Zaktualizowane powiadomienie.
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

}