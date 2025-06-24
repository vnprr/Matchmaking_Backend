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

    /**
     * Pobiera listę konwersacji użytkownika.
     */
    @GetMapping("/conversations")
    public Page<ConversationDTO> getUserConversations(Pageable pageable) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.getUserConversations(currentProfileId, pageable);
    }

    /**
     * Pobiera wiadomości z danej konwersacji.
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Page<MessageDTO> getConversationMessages(
            @PathVariable Long conversationId,
            Pageable pageable) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.getConversationMessages(conversationId, currentProfileId, pageable);
    }

    /**
     * Wysyła wiadomość do innego użytkownika na podstawie ID profilu odbiorcy.
     */
    @PostMapping("/conversations/profile/{recipientProfileId}")
    public MessageDTO sendMessage(
            @PathVariable Long recipientProfileId,
            @Valid @RequestBody MessageRequest messageRequest) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        return chatService.sendMessage(recipientProfileId, messageRequest, currentProfileId);
    }

    /**
     * Oznacza konwersację jako przeczytaną.
     */
    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long conversationId) {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        chatService.markConversationAsRead(conversationId, currentProfileId);
        return ResponseEntity.ok().build();
    }

    /**
     * Pobiera liczbę nieprzeczytanych wiadomości dla bieżącego użytkownika.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessagesCount() {
        Long currentProfileId = userService.getCurrentUser().getProfile().getId();
        long unreadCount = chatService.getUnreadMessagesCount(currentProfileId);
        return ResponseEntity.ok(unreadCount);
    }
}