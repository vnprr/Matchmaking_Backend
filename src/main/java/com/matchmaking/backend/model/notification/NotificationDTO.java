package com.matchmaking.backend.model.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String photoUrl;
    private boolean read;
    private String content;
    private LocalDateTime createdAt;

    private NotificationType type;
    private Long targetId; // ID zasobu docelowego
}
