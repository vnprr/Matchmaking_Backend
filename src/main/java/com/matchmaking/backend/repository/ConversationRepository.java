package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.chat.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;


import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.firstUser = :profile OR c.secondUser = :profile) " +
            "ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findConversationsByProfile(UserProfile profile, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.firstUser = :firstProfile AND c.secondUser = :secondProfile) OR " +
            "(c.firstUser = :secondProfile AND c.secondUser = :firstProfile)")
    Optional<Conversation> findConversationBetweenProfiles(UserProfile firstProfile, UserProfile secondProfile);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation IN " +
            "(SELECT c FROM Conversation c WHERE c.firstUser = :profile OR c.secondUser = :profile) " +
            "AND m.recipient = :profile AND m.read = false")
    long countUnreadMessagesByProfile(UserProfile profile);
}