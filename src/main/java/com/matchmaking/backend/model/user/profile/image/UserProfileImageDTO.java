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
    private String profileUrl;
    private boolean isMain;
    private int displayOrder;
}