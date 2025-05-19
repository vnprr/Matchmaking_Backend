package com.matchmaking.backend.model.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private boolean isOwnMessage;
}