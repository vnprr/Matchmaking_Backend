package com.matchmaking.backend.model.user.profile.image;

import lombok.Data;

@Data
public class ImageCropDTO {
    private int x;
    private int y;
    private int width;
    private int height;
}