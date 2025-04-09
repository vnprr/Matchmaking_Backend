package com.matchmaking.backend.model.user.profile.section;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile_section_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfileSectionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;         // np. "Zainteresowania"

    @Column(nullable = false)
    private int displayOrder;    // kolejność wyświetlania

    @Column(nullable = false)
    private boolean required;    // czy wymagane

    @Column(nullable = false)
    private boolean visible;     // czy widoczne

    @Column(columnDefinition = "TEXT")
    private String description;  // opis sekcji / wskazówka dla użytkownika

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
}