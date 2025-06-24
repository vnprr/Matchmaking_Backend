package com.matchmaking.backend.model.image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserProfileImageOrderDTO {
    @NotEmpty(message = "Lista zdjęć nie może być pusta!")
    private List<@Valid ImageOrderItem> images;

    @Data
    public static class ImageOrderItem {
        @NotNull(message = "Identyfikator zdjęcia musi być określony!")
        private Long id;

        @NotNull(message = "Kolejność wyświetlania musi być określona!")
        private Integer displayOrder;
    }
}