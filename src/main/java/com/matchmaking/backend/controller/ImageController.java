package com.matchmaking.backend.controller;

import com.matchmaking.backend.exception.ResourceNotFoundException;
import com.matchmaking.backend.model.user.profile.image.ImageCropDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageOrderDTO;
import com.matchmaking.backend.service.user.profile.image.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/me/images/avatar")
    public ResponseEntity<UserProfileImageDTO> getCurrentUserAvatarImage() {
        return ResponseEntity.ok(imageService.getAvatarProfileImage(null));
    }

    @GetMapping("/me/images/all")
    public ResponseEntity<List<UserProfileImageDTO>> getCurrentUserImages() {
        return ResponseEntity.ok(imageService.getProfileImages(null));
    }

    @GetMapping("/{profileId}/images/avatar")
    public ResponseEntity<UserProfileImageDTO> getProfileAvatarImage(@PathVariable Long profileId) {
        return ResponseEntity.ok(imageService.getAvatarProfileImage(profileId));
    }

    @GetMapping("/{profileId}/images/all")
    public ResponseEntity<List<UserProfileImageDTO>> getProfileImages(@PathVariable Long profileId) {
        return ResponseEntity.ok(imageService.getProfileImages(profileId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            UserProfileImageDTO result = imageService.uploadImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas wgrywania zdjęcia: " + e.getMessage());
        }
    }

    /**
     * Kadruje zdjęcie do określonego obszaru.
     * W przeciwieństwie do metody setAvatarImage tutaj nie ma wymogu, aby kadrowanie było kwadratowe.
     * 
     * @param imageId ID zdjęcia do kadrowania
     * @param cropDTO parametry kadrowania (x, y, width, height)
     * @return zaktualizowane zdjęcie lub komunikat o błędzie
     */
    @PostMapping("/images/{imageId}/crop")
    public ResponseEntity<?> cropImage(
            @PathVariable Long imageId,
            @Valid @RequestBody ImageCropDTO cropDTO) {
        try {
            UserProfileImageDTO result = imageService.cropImage(imageId, cropDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas przycinania zdjęcia: " + e.getMessage());
        }
    }

    /**
     * Ustawia zdjęcie jako avatar i kadruje je do kwadratu.
     * 
     * Współrzędne (x,y) są liczone od lewego górnego rogu obrazka.
     * System sprawdza, czy x+width lub y+height nie przekraczają wymiarów oryginalnego obrazka.
     * 
     * @param imageId ID zdjęcia do ustawienia jako avatar
     * @param cropDTO parametry kadrowania (x, y, width, height)
     * @return zaktualizowane zdjęcie lub komunikat o błędzie
     */
    @PatchMapping("/images/{imageId}/avatar")
    public ResponseEntity<?> setAvatarImage(@PathVariable Long imageId, @Valid @RequestBody ImageCropDTO cropDTO) {
        try {
            // Sprawdź czy kadrowanie jest kwadratowe (1:1)
            if (!cropDTO.isSquare()) {
                return ResponseEntity.badRequest().body("Kadrowanie dla zdjęcia avatara musi być kwadratowe (1:1)");
            }

            UserProfileImageDTO result = imageService.setAvatarImage(imageId, cropDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas ustawiania zdjęcia jako avatar: " + e.getMessage());
        }
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas usuwania zdjęcia: " + e.getMessage());
        }
    }

    @PutMapping("/images/order")
    public ResponseEntity<?> updateImagesOrder(@Valid @RequestBody UserProfileImageOrderDTO orderDTO) {
        try {
            imageService.updateImagesOrder(orderDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<?> getImage(@PathVariable Long imageId) {
        try {
            UserProfileImageDTO imageDTO = imageService.getImageById(imageId);
            return ResponseEntity.ok(imageDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas pobierania zdjęcia: " + e.getMessage());
        }
    }
}
