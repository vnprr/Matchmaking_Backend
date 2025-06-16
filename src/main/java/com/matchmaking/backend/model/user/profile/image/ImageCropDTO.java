package com.matchmaking.backend.model.user.profile.image;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for image cropping operations.
 * For avatar images, the aspect ratio is enforced to be 1:1 (square).
 * The backend will automatically scale the image to 512x512 pixels.
 * 
 * The coordinate system uses (x,y) measured from the top-left corner of the image.
 * 
 * Note: The application does not explicitly validate whether x+width or y+height
 * exceed the dimensions of the original image. It is recommended to ensure that
 * the cropping region stays within the image boundaries.
 */
@Data
public class ImageCropDTO {
    @NotNull(message = "Pozycja X musi być określona")
    private Integer x;

    @NotNull(message = "Pozycja Y musi być określona")
    private Integer y;

    @NotNull(message = "Szerokość musi być określona")
    @Min(value = 10, message = "Szerokość musi być co najmniej 10px")
    private Integer width;

    @NotNull(message = "Wysokość musi być określona")
    @Min(value = 10, message = "Wysokość musi być co najmniej 10px")
    private Integer height;

    /**
     * Validates that the crop dimensions form a square (1:1 aspect ratio).
     * This is required for avatar images.
     * 
     * @return true if the crop is square, false otherwise
     */
    public boolean isSquare() {
        return width != null && height != null && width.equals(height);
    }
}
