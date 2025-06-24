package com.matchmaking.backend.model.chat;

import com.matchmaking.backend.model.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserProfile sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserProfile recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean read;

    @Column
    private LocalDateTime readAt;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}