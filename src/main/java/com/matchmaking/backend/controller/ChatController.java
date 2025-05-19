package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.chat.ConversationDTO;
import com.matchmaking.backend.model.chat.MessageDTO;
import com.matchmaking.backend.model.chat.MessageRequest;
import com.matchmaking.backend.service.UserService;
import com.matchmaking.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/conversations")
    public Page<ConversationDTO> getUserConversations(Pageable pageable) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.getUserConversations(currentProfileId, pageable);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Page<MessageDTO> getConversationMessages(
            @PathVariable Long conversationId,
            Pageable pageable) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.getConversationMessages(conversationId, currentProfileId, pageable);
    }

    @PostMapping("/conversations/profile/{recipientProfileId}")
    public MessageDTO sendMessage(
            @PathVariable Long recipientProfileId,
            @Valid @RequestBody MessageRequest messageRequest) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.sendMessage(recipientProfileId, messageRequest, currentProfileId);
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long conversationId) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        chatService.markConversationAsRead(conversationId, currentProfileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessagesCount() {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        long unreadCount = chatService.getUnreadMessagesCount(currentProfileId);
        return ResponseEntity.ok(unreadCount);
    }
}