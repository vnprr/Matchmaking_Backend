package com.matchmaking.backend.service.user.profile.image;

import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.image.ImageCropDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImage;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageOrderDTO;
import com.matchmaking.backend.repository.UserProfileImageRepository;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final UserProfileImageRepository imageRepository;

    private static final int MAX_IMAGES_PER_USER = 10;

    @Transactional(readOnly = true)
    public UserProfileImageDTO getCurrentUserProfileImage() {
        User user = getCurrentUser();
        return imageRepository.findByUserProfileAndIsMainTrue(user.getProfile())
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserProfileImageDTO> getUserProfileImages() {
        User user = getCurrentUser();
        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(user.getProfile())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileImageDTO uploadProfileImage(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Sprawdź limit zdjęć
        List<UserProfileImage> existingImages = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        if (existingImages.size() >= MAX_IMAGES_PER_USER) {
            throw new IllegalStateException("Osiągnięto maksymalną liczbę zdjęć (" + MAX_IMAGES_PER_USER + ")");
        }

        // Wgraj zdjęcie do Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);

        // Ustal kolejność zdjęcia (ostatnie)
        Integer maxOrder = imageRepository.findMaxDisplayOrder(profile);
        int newOrder = (maxOrder != null) ? maxOrder + 1 : 0;

        // Utwórz nowy rekord
        UserProfileImage image = new UserProfileImage();
        image.setUserProfile(profile);
        image.setPublicId((String) uploadResult.get("public_id"));
        image.setImageUrl((String) uploadResult.get("url"));
        image.setMain(existingImages.isEmpty()); // Pierwsze zdjęcie jest automatycznie główne
        image.setDisplayOrder(newOrder);

        UserProfileImage savedImage = imageRepository.save(image);
        return mapToDTO(savedImage);
    }

    @Transactional
    public void deleteProfileImage(Long imageId) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        UserProfileImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Zdjęcie nie istnieje"));

        // Sprawdź czy zdjęcie należy do użytkownika
        if (!image.getUserProfile().getId().equals(profile.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do tego zdjęcia");
        }

        // Usuń oryginalne zdjęcie z Cloudinary
        cloudinaryService.deleteImage(image.getPublicId());

        // Jeśli istnieje przycięta wersja, usuń również ją
        if (image.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(image.getProfileImagePublicId());
        }

        // Jeśli usuwane zdjęcie było główne, ustaw inne jako główne
        if (image.isMain()) {
            List<UserProfileImage> remainingImages = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
            remainingImages.remove(image);
            if (!remainingImages.isEmpty()) {
                UserProfileImage newMain = remainingImages.get(0);
                newMain.setMain(true);
                imageRepository.save(newMain);
            }
        }

        // Usuń zdjęcie z bazy
        imageRepository.delete(image);

        // Przenumeruj pozostałe zdjęcia
        updateImagesOrder(profile);
    }

    @Transactional
    public void setMainProfileImage(Long imageId) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Znajdź zdjęcie, które ma być główne
        UserProfileImage newMainImage = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Zdjęcie nie istnieje"));

        // Sprawdź czy zdjęcie należy do użytkownika
        if (!newMainImage.getUserProfile().getId().equals(profile.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do tego zdjęcia");
        }

        // Znajdź aktualne główne zdjęcie i zmień jego status
        imageRepository.findByUserProfileAndIsMainTrue(profile)
                .ifPresent(currentMain -> {
                    currentMain.setMain(false);
                    imageRepository.save(currentMain);
                });

        // Ustaw nowe zdjęcie jako główne
        newMainImage.setMain(true);
        imageRepository.save(newMainImage);
    }

    @Transactional
    public UserProfileImageDTO cropAndSetProfileImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        UserProfileImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Zdjęcie nie istnieje"));

        // Sprawdź czy zdjęcie należy do użytkownika
        if (!image.getUserProfile().getId().equals(profile.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do tego zdjęcia");
        }

        // Przygotuj parametry przycinania dla Cloudinary
        Map<String, Object> cropParams = new HashMap<>();
        cropParams.put("x", cropDTO.getX());
        cropParams.put("y", cropDTO.getY());
        cropParams.put("width", cropDTO.getWidth());
        cropParams.put("height", cropDTO.getHeight());
        cropParams.put("crop", "crop");

        // Usuń istniejące przycięte zdjęcie, jeśli istnieje
        if (image.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(image.getProfileImagePublicId());
        }

        // Wykonaj przycinanie przez Cloudinary
        Map<String, Object> croppedResult = cloudinaryService.transformImage(image.getPublicId(), cropParams);

        // Aktualizuj dane zdjęcia
        image.setProfileImagePublicId((String) croppedResult.get("public_id"));
        image.setProfileImageUrl((String) croppedResult.get("url"));

        // Ustaw jako główne jeśli nie jest
        if (!image.isMain()) {
            imageRepository.findByUserProfileAndIsMainTrue(profile)
                    .ifPresent(currentMain -> {
                        currentMain.setMain(false);
                        imageRepository.save(currentMain);
                    });
            image.setMain(true);
        }

        return mapToDTO(imageRepository.save(image));
    }

    @Transactional
    public void updateProfileImagesOrder(UserProfileImageOrderDTO orderDTO) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        Map<Long, UserProfileImage> imageMap = images.stream()
                .collect(Collectors.toMap(UserProfileImage::getId, img -> img));

        for (UserProfileImageOrderDTO.ImageOrderItem item : orderDTO.getImages()) {
            UserProfileImage image = imageMap.get(item.getId());
            if (image != null) {
                image.setDisplayOrder(item.getDisplayOrder());
            }
        }

        imageRepository.saveAll(imageMap.values());
    }

    private void updateImagesOrder(UserProfile profile) {
        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setDisplayOrder(i);
        }
        imageRepository.saveAll(images);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
        UserProfileImageDTO dto = new UserProfileImageDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setProfileImageUrl(image.getProfileImageUrl());
        dto.setMain(image.isMain());
        dto.setDisplayOrder(image.getDisplayOrder());
        return dto;
    }
}