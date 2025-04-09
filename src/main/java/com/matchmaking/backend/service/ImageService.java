package com.matchmaking.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.*;
import com.matchmaking.backend.model.user.profile.image.UserProfileImage;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageDTO;
import com.matchmaking.backend.model.user.profile.image.UserProfileImageOrderDTO;
import com.matchmaking.backend.repository.UserProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    /**
    Serwis zdjęć profilu użytkownika
    Obsługuje:
    - uploadProfileImage - przesyłanie zdjęcia do Cloudinary
    - deleteProfileImage - usuwanie zdjęcia z Cloudinary i bazy danych
    - setMainProfileImage - ustawianie zdjęcia jako główne
     */

    private final Cloudinary cloudinary;
    private final UserService userService;
    private final UserProfileImageRepository imageRepository;

    // to przeniesc do parametrow
    private static final int MAX_IMAGES_PER_PROFILE = 6;

    /**
     Przesyłanie zdjęcia do Cloudinary
        @param file MultipartFile - zdjęcie do przesłania
        @return UserProfileImageDTO - obiekt DTO z informacjami o przesłanym zdjęciu
     */
    @Transactional
    public UserProfileImageDTO uploadProfileImage(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Sprawdź limit zdjęć
        int currentImagesCount = imageRepository.countByUserProfile(profile);
        if (currentImagesCount >= MAX_IMAGES_PER_PROFILE) {
            throw new IllegalStateException("Maksymalna liczba zdjęć (" + MAX_IMAGES_PER_PROFILE + ") została osiągnięta");
        }

        // Wgraj do Cloudinary z automatyczną optymalizacją i transformacją
        Map uploadResult = cloudinary
                .uploader()
                .upload(file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "user_profiles",
                                "transformation", "q_auto,f_auto",
                                "width", 800,
                                "crop", "limit"
                        ));

        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        // Jeśli to pierwsze zdjęcie, ustaw jako główne
        boolean isMain = currentImagesCount == 0;

        // Ustal kolejność wyświetlania (ostatnia pozycja)
        int displayOrder = currentImagesCount + 1;

        UserProfileImage image = new UserProfileImage();

        image.setImageUrl(imageUrl);
        image.setPublicId(publicId);
        image.setUserProfile(profile);
        image.setMain(isMain);
        image.setDisplayOrder(displayOrder);
        image.setCreatedAt(LocalDateTime.now());
        image.setUpdatedAt(LocalDateTime.now());

        UserProfileImage savedImage = imageRepository.save(image);

        return mapToDTO(savedImage);
    }

    @Transactional
    public void deleteProfileImage(Long imageId) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        UserProfileImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Zdjęcie nie istnieje"));

        // Sprawdź czy zdjęcie należy do zalogowanego użytkownika
        if (!image.getUserProfile().getId().equals(profile.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do usunięcia tego zdjęcia");
        }

        // Usuń z Cloudinary
        cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());

        // Usuń z bazy danych
        imageRepository.delete(image);

        // Jeśli usunięto zdjęcie główne, ustaw nowe zdjęcie główne (jeśli istnieją inne zdjęcia)
        if (image.isMain()) {
            List<UserProfileImage> remainingImages = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
            if (!remainingImages.isEmpty()) {
                UserProfileImage newMainImage = remainingImages.get(0);
                newMainImage.setMain(true);
                imageRepository.save(newMainImage);
            }
        }

        // Aktualizacja kolejności pozostałych zdjęć
        updateImagesOrder(profile);
    }

    @Transactional
    public void setMainProfileImage(Long imageId) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Znajdź obecne główne zdjęcie i wyłącz flagę isMain
        imageRepository.findByUserProfileAndIsMainTrue(profile).ifPresent(currentMain -> {
            currentMain.setMain(false);
            imageRepository.save(currentMain);
        });

        // Ustaw nowe główne zdjęcie
        UserProfileImage newMain = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Zdjęcie nie istnieje"));

        if (!newMain.getUserProfile().getId().equals(profile.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do modyfikacji tego zdjęcia");
        }

        newMain.setMain(true);
        imageRepository.save(newMain);
    }

    @Transactional
    public void updateProfileImagesOrder(UserProfileImageOrderDTO orderDTO) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        for (UserProfileImageOrderDTO.ImageOrderItem item : orderDTO.getImages()) {
            UserProfileImage image = imageRepository.findById(item.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Zdjęcie o ID " + item.getId() + " nie istnieje"));

            if (!image.getUserProfile().getId().equals(profile.getId())) {
                throw new IllegalStateException("Nie masz uprawnień do modyfikacji zdjęcia o ID " + item.getId());
            }

            image.setDisplayOrder(item.getDisplayOrder());
            imageRepository.save(image);
        }
    }

    public List<UserProfileImageDTO> getUserProfileImages() {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
        UserProfileImageDTO dto = new UserProfileImageDTO();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setDisplayOrder(image.getDisplayOrder());
        dto.setMain(image.isMain());
        return dto;
    }

    private void updateImagesOrder(UserProfile profile) {
        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        for (int i = 0; i < images.size(); i++) {
            UserProfileImage image = images.get(i);
            image.setDisplayOrder(i + 1);
            imageRepository.save(image);
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}