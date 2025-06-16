package com.matchmaking.backend.model.user.profile.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
public class UserProfileImageDTO {
    private Long id;
    private String originalUrl;
    private String galleryUrl;
    private String thumbnailUrl;
    private String avatarUrl;
    private boolean isAvatar;
    private int displayOrder;

    // Image dimensions
    private Integer originalWidth;
    private Integer originalHeight;
    private Integer galleryWidth;
    private Integer galleryHeight;
    private Integer thumbnailWidth;
    private Integer thumbnailHeight;
    private Integer avatarWidth;
    private Integer avatarHeight;
}
