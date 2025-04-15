package com.matchmaking.backend.model.user.profile.image;

import com.matchmaking.backend.model.UserProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    // Oryginalne zdjęcie
    private String publicId;
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // Przycięte zdjęcie profilowe (jeśli ten obrazek jest użyty jako zdjęcie profilowe)
    private String profileImagePublicId;
    private String profileImageUrl;

    // Czy to jest główne zdjęcie w galerii
    private boolean isMain;

    // Kolejność wyświetlania
    private int displayOrder;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
}