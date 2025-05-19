package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.chat.Conversation;
import com.matchmaking.backend.model.chat.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.recipient = :userProfile AND m.read = false")
    long countUnreadMessagesInConversation(Conversation conversation, UserProfile userProfile);

    List<Message> findByConversationAndRecipientAndReadFalse(Conversation conversation, UserProfile recipient);
}