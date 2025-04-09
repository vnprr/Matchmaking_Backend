package com.matchmaking.backend.model.user.profile.image;

import lombok.Data;

@Data
public class UserProfileImageDTO {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private boolean isMain;
}