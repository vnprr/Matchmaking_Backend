package com.matchmaking.backend.model.user.profile.image;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileImageOrderDTO {
    private List<ImageOrderItem> images;

    @Data
    public static class ImageOrderItem {
        private Long id;
        private Integer displayOrder;
    }

}