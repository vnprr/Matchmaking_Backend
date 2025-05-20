package com.matchmaking.backend.controller;

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

    @GetMapping("/me/images/main")
    public ResponseEntity<UserProfileImageDTO> getCurrentUserMainImage() {
        return ResponseEntity.ok(imageService.getMainProfileImage(null));
    }

    @GetMapping("/me/images/all")
    public ResponseEntity<List<UserProfileImageDTO>> getCurrentUserImages() {
        return ResponseEntity.ok(imageService.getProfileImages(null));
    }

    @GetMapping("/{profileId}/images/main")
    public ResponseEntity<UserProfileImageDTO> getProfileMainImage(@PathVariable Long profileId) {
        return ResponseEntity.ok(imageService.getMainProfileImage(profileId));
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

    @PostMapping("/images/{imageId}/crop")
    public ResponseEntity<?> cropImage(@PathVariable Long imageId, @Valid @RequestBody ImageCropDTO cropDTO) {
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

    @PatchMapping("/images/{imageId}/main")
    public ResponseEntity<?> setMainImage(@PathVariable Long imageId) {
        try {
            UserProfileImageDTO result = imageService.setMainImage(imageId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas ustawiania zdjęcia jako główne: " + e.getMessage());
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
}

//package com.matchmaking.backend.controller;
//
//import com.matchmaking.backend.exception.ErrorResponse;
//import com.matchmaking.backend.model.user.profile.image.ImageCropDTO;
//import com.matchmaking.backend.model.user.profile.image.UserProfileImageDTO;
//import com.matchmaking.backend.model.user.profile.image.UserProfileImageOrderDTO;
//import com.matchmaking.backend.service.user.profile.image.ImageService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/profile/images")
//@RequiredArgsConstructor
//public class ImageController {
//
//    private final ImageService imageService;
//
//    @GetMapping("/current/main")
//    public ResponseEntity<UserProfileImageDTO> getCurrentUserMainImage() {
//        return ResponseEntity.ok(imageService.getMainProfileImage(null));
//    }
//
//    @GetMapping("/current/all")
//    public ResponseEntity<List<UserProfileImageDTO>> getCurrentUserImages() {
//        return ResponseEntity.ok(imageService.getProfileImages(null));
//    }
//
//    @GetMapping("/profile/{profileId}/main")
//    public ResponseEntity<UserProfileImageDTO> getProfileMainImage(@PathVariable Long profileId) {
//        return ResponseEntity.ok(imageService.getMainProfileImage(profileId));
//    }
//
//    @GetMapping("/profile/{profileId}/all")
//    public ResponseEntity<List<UserProfileImageDTO>> getProfileImages(@PathVariable Long profileId) {
//        return ResponseEntity.ok(imageService.getProfileImages(profileId));
//    }
//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
//        try {
//            UserProfileImageDTO result = imageService.uploadImage(file);
//            return ResponseEntity.status(HttpStatus.CREATED).body(result);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        } catch (IOException e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Błąd podczas wgrywania zdjęcia: " + e.getMessage()));
//        }
//    }
//
//    @PostMapping("/{imageId}/crop")
//    public ResponseEntity<?> cropImage(@PathVariable Long imageId, @Valid @RequestBody ImageCropDTO cropDTO) {
//        try {
//            UserProfileImageDTO result = imageService.cropImage(imageId, cropDTO);
//            return ResponseEntity.ok(result);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        } catch (IOException e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Błąd podczas przycinania zdjęcia: " + e.getMessage()));
//        }
//    }
//
//    @PatchMapping("/{imageId}/main")
//    public ResponseEntity<?> setMainImage(@PathVariable Long imageId) {
//        try {
//            UserProfileImageDTO result = imageService.setMainImage(imageId);
//            return ResponseEntity.ok(result);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        } catch (IOException e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Błąd podczas ustawiania zdjęcia jako główne: " + e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{imageId}")
//    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
//        try {
//            imageService.deleteImage(imageId);
//            return ResponseEntity.noContent().build();
//        } catch (IOException e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse("Błąd podczas usuwania zdjęcia: " + e.getMessage()));
//        }
//    }
//
//    @PutMapping("/order")
//    public ResponseEntity<?> updateImagesOrder(@Valid @RequestBody UserProfileImageOrderDTO orderDTO) {
//        try {
//            imageService.updateImagesOrder(orderDTO);
//            return ResponseEntity.ok().build();
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
//}