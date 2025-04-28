package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.user.profile.image.ImageCropDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageOrderDTO;
import com.matchmaking.backend.service.user.profile.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/profile/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Zdjęcie profilowe zalogowanego użytkownika.
     * @return DTO zdjęcia profilowego.
     */
    @GetMapping("/main")
    public ResponseEntity<UserProfileImageDTO> getCurrentUserProfileImage() {
        return ResponseEntity.ok(imageService.getCurrentUserProfileImage());
    }

    /**
     * Lista wszystkich zdjęć zalogowanego użytkownika.
     * @return Lista z DTO zdjęć użytkownika.
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserProfileImageDTO>> getCurrentUserProfileImages() {
        return ResponseEntity.ok(imageService.getUserProfileImages());
    }

    /**
     * Zdjęcie profilowe użytkownika o podanym ID.
     * @return DTO zdjęcia profilowego.
     */
    @GetMapping("/user/{userId}/main")
    public ResponseEntity<UserProfileImageDTO> getUserProfileImage(@PathVariable Long userId) {
        return ResponseEntity.ok(imageService.getUserProfileImageById(userId));
    }

    /**
     * Lista wszystkich zdjęć użytkownika o podanym ID.
     * @return Lista z DTO zdjęć użytkownika.
     */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<UserProfileImageDTO>> getUserProfileImages(@PathVariable Long userId) {
        return ResponseEntity.ok(imageService.getUserImagesById(userId));
    }

    /**
     * Wgrywanie zdjęcia.
     */
    @PostMapping
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            UserProfileImageDTO imageDTO = imageService.uploadProfileImage(file);
            return ResponseEntity.ok(imageDTO);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nie udało się wgrać zdjęcia: " + e.getMessage());
        }
    }

    /**
     * Usuwanie zdjęcia.
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long imageId) {
        try {
            imageService.deleteProfileImage(imageId);
            return ResponseEntity.ok("Zdjęcie zostało usunięte");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nie udało się usunąć zdjęcia: " + e.getMessage());
        }
    }

    @PostMapping("/{imageId}/crop")
    public ResponseEntity<?> cropProfileImage(
            @PathVariable Long imageId,
            @RequestBody ImageCropDTO cropDTO) {
        try {
            UserProfileImageDTO imageDTO = imageService.cropAndSetProfileImage(imageId, cropDTO);
            return ResponseEntity.ok(imageDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Nie udało się przyciąć zdjęcia: " + e.getMessage());
        }
    }

    @PutMapping("/order")
    public ResponseEntity<?> updateProfileImagesOrder(@RequestBody UserProfileImageOrderDTO orderDTO) {
        try {
            imageService.updateProfileImagesOrder(orderDTO);
            return ResponseEntity.ok("Kolejność zdjęć została zaktualizowana");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}