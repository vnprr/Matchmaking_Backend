package com.matchmaking.backend.model.chat;

import com.matchmaking.backend.model.profile.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "first_user_id", nullable = false)
    private UserProfile firstUser;

    @ManyToOne
    @JoinColumn(name = "second_user_id", nullable = false)
    private UserProfile secondUser;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;
}