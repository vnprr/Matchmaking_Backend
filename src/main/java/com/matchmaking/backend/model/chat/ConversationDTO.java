package com.matchmaking.backend.model.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationDTO {
    private Long id;
    private Long recipientId;
    private String recipientName;
    private String recipientPhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private boolean hasUnreadMessages;
    private int unreadCount;
}