// src/main/java/com/matchmaking/backend/model/user/profile/image/UserProfileImageDTO.java
package com.matchmaking.backend.model.user.profile.image;

import lombok.Data;

@Data
public class UserProfileImageDTO {
    private Long id;
    private String imageUrl;         // Oryginał
    private String profileImageUrl;  // Wersja profilowa (512x512, 1:1)
    private boolean isMain;
    private int displayOrder;
}