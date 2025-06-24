package com.matchmaking.backend.model.image;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageCropDTO {
    @NotNull(message = "Pozycja X musi być określona!")
    private Integer x;

    @NotNull(message = "Pozycja Y musi być określona!")
    private Integer y;

    @NotNull(message = "Szerokość musi być określona!")
    @Min(value = 10, message = "Szerokość musi być co najmniej 10px!")
    private Integer width;

    @NotNull(message = "Wysokość musi być określona!")
    @Min(value = 10, message = "Wysokość musi być co najmniej 10px!")
    private Integer height;

    /**
     * Sprawdza, czy przycięcie jest kwadratowe.
     * @return <ul><li>true, jeśli szerokość i wysokość są równe,</li><li>false, w przeciwnym razie.</li></ul>
     */
    public boolean isSquare() {
        return width != null && height != null && width.equals(height);
    }
}
