package com.matchmaking.backend.model.user.profile.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.matchmaking.backend.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonIgnore
    private UserProfile userProfile;

    // Oryginalne zdjęcie
    private String publicId;

    @Column(nullable = false)
    private String originalUrl;

    // URL do obrazu w jakości galerii (zoptymalizowany)
    @Column(nullable = false)
    private String galleryUrl;

    // URL do miniatury (thumbnail)
    @Column(nullable = false)
    private String thumbnailUrl;

    // URL do przyciętego zdjęcia avatara
    private String avatarUrl;

    @Column(nullable = false)
    private boolean isAvatar;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
