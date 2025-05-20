package com.matchmaking.backend.model.user.profile.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImageVersionsDTO {
    private final String publicId;
    private final String originalUrl;
    private final String galleryUrl;
    private final String thumbnailUrl;

}
