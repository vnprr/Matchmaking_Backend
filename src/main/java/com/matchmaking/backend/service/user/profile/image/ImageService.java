package com.matchmaking.backend.service.user.profile.image;

import com.matchmaking.backend.exception.ResourceNotFoundException;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.image.*;
import com.matchmaking.backend.repository.UserProfileImageRepository;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;
    private final CloudinaryService cloudinaryService;
    private final UserProfileImageRepository imageRepository;

    @Value("${app.profile.max-images:10}")
    private int maxImagesPerUser;

    @Transactional(readOnly = true)
    public UserProfileImageDTO getAvatarProfileImage(
            Long userProfileId
    ) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileAndIsAvatarTrue(profile)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserProfileImageDTO> getProfileImages(
            Long userProfileId
    ) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public UserProfileImageDTO uploadImage(
            MultipartFile file
    ) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Sprawdź limit zdjęć
        List<UserProfileImage> existingImages = imageRepository.findByUserProfile(profile);
        if (existingImages.size() >= maxImagesPerUser) {
            throw new IllegalStateException("Osiągnięto maksymalną liczbę zdjęć (" + maxImagesPerUser + ")");
        }

        ImageVersionsDTO versions = cloudinaryService.uploadImage(file);

        // Ustal kolejność nowego zdjęcia
        Integer maxOrder = imageRepository.findMaxDisplayOrder(profile);
        int displayOrder = (maxOrder == null) ? 0 : maxOrder + 1;

        // Zapisz w bazie danych
        UserProfileImage image = UserProfileImage.builder()
                .userProfile(profile)
                .publicId(versions.getPublicId())
                .originalUrl(versions.getOriginalUrl())
                .galleryUrl(versions.getGalleryUrl())
                .thumbnailUrl(versions.getThumbnailUrl())
                .displayOrder(displayOrder)
                .isAvatar(existingImages.isEmpty()) // Pierwsze zdjęcie staje się avatarem
                .build();

        return mapToDTO(imageRepository.save(image));
    }

    /**
     * Kadruje zdjęcie o podanym identyfikatorze.
     * @param imageId identyfikator zdjęcia
     * @param cropDTO DTO z parametrami kadrowania
     * @return DTO ze zdjęciem (z zaktualizowanymi URL-ami galerii i miniaturki)
     */
    @Transactional
    public UserProfileImageDTO cropImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        User user = userService.getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        // Generujemy nowe URL-e
        String galleryUrl = cloudinaryService.cropToGallery(
                image.getPublicId(),
                cropDTO.getX(), cropDTO.getY(),
                cropDTO.getWidth(), cropDTO.getHeight()
        );
        String thumbnailUrl = cloudinaryService.cropToThumbnail(
                image.getPublicId(),
                cropDTO.getX(), cropDTO.getY(),
                cropDTO.getWidth(), cropDTO.getHeight()
        );

        // Nadpisujemy w encji i zapisujemy
        image.setGalleryUrl(galleryUrl);
        image.setThumbnailUrl(thumbnailUrl);
        imageRepository.save(image);

        return mapToDTO(image);
    }

    /**
     * Kadruje zdjęcie do kwadratowego avatara, nadpisuje URL avatara.
     * @param imageId identyfikator zdjęcia
     * @param cropDTO DTO z parametrami kadrowania
     * @return DTO ze zdjęciem (z zaktualizowanym URL-em avatara)
     */
    @Transactional
    public UserProfileImageDTO setAvatarImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        if (!cropDTO.isSquare()) {
            throw new IllegalStateException("Kadrowanie dla avatara musi być kwadratowe (1:1)");
        }

        User user = userService.getCurrentUser();
        UserProfileImage newAvatar = getImageAndCheckPermission(imageId, user);

        // generujemy URL avatara i nadpisujemy w DB
        String avatarUrl = cloudinaryService.cropToAvatar(
                newAvatar.getPublicId(),
                cropDTO.getX(), cropDTO.getY(),
                cropDTO.getWidth(), cropDTO.getHeight()
        );
        newAvatar.setAvatarUrl(avatarUrl);

        // odznacz poprzedni avatar
        imageRepository.findByUserProfileAndIsAvatarTrue(user.getProfile())
                .ifPresent(old -> {
                    old.setAvatar(false);
                    imageRepository.save(old);
                });

        newAvatar.setAvatar(true);
        imageRepository.save(newAvatar);

        return mapToDTO(newAvatar);
    }

    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        User user = getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        cloudinaryService.deleteImage(image.getPublicId());

        imageRepository.delete(image);

        reorderImages(user.getProfile());
    }

    @Transactional
    public void updateImagesOrder(UserProfileImageOrderDTO orderDTO) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        List<UserProfileImage> userImages = imageRepository.findByUserProfile(profile);
        Map<Long, UserProfileImage> imageMap = userImages.stream()
                .collect(Collectors.toMap(UserProfileImage::getId, img -> img));

        for (UserProfileImageOrderDTO.ImageOrderItem item : orderDTO.getImages()) {
            UserProfileImage image = imageMap.get(item.getId());
            if (image != null) {
                image.setDisplayOrder(item.getDisplayOrder());
            } else {
                throw new ResourceNotFoundException("Zdjęcie o ID " + item.getId() + " nie istnieje lub nie należy do tego użytkownika");
            }
        }

        imageRepository.saveAll(imageMap.values());
    }

    private void reorderImages(UserProfile profile) {
        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        for (int i = 0; i < images.size(); i++) {
            UserProfileImage img = images.get(i);
            img.setDisplayOrder(i);
        }
        imageRepository.saveAll(images);
    }

    private UserProfileImage getImageAndCheckPermission(
            Long imageId,
            User user
    ) {
        UserProfileImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Zdjęcie o ID " + imageId + " nie istnieje"));

        if (!image.getUserProfile().getId().equals(user.getProfile().getId())) {
            throw new ResourceNotFoundException("Zdjęcie o ID " + imageId + " nie należy do tego użytkownika");
        }

        return image;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

    private UserProfile getUserProfileById(Long profileId) {
        if (profileId == null) {
            return getCurrentUser().getProfile();
        }
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil o ID " + profileId + " nie istnieje"));
    }

    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
        UserProfileImageDTO.UserProfileImageDTOBuilder builder = UserProfileImageDTO.builder()
                .id(image.getId())
                .originalUrl(image.getOriginalUrl())
                .galleryUrl(image.getGalleryUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .avatarUrl(image.getAvatarUrl())
                .isAvatar(image.isAvatar())
                .displayOrder(image.getDisplayOrder());

        try {
            Integer originalWidth = null;
            Integer originalHeight = null;
            if (image.getPublicId() != null) {
                Map<String, Integer> originalDimensions = cloudinaryService.getImageDimensions(image.getPublicId());
                originalWidth = originalDimensions.get("width");
                originalHeight = originalDimensions.get("height");
                builder.originalWidth(originalWidth)
                       .originalHeight(originalHeight);
            }

            if (image.getGalleryUrl() != null) {
                builder.galleryWidth(CloudinaryService.GALLERY_WIDTH);
                if (originalWidth != null && originalHeight != null) {
                    int galleryHeight = (CloudinaryService.GALLERY_WIDTH * originalHeight) / originalWidth;
                    builder.galleryHeight(galleryHeight);
                }
            }

            if (image.getThumbnailUrl() != null) {
                builder.thumbnailWidth(CloudinaryService.THUMBNAIL_SIZE)
                       .thumbnailHeight(CloudinaryService.THUMBNAIL_SIZE);
            }

            if (image.getAvatarUrl() != null) {
                builder.avatarWidth(CloudinaryService.AVATAR_SIZE)
                       .avatarHeight(CloudinaryService.AVATAR_SIZE);
            }
        } catch (Exception e) {
            System.err.println("Error getting image dimensions: " + e.getMessage());
        }

        return builder.build();
    }

    @Transactional(readOnly = true)
    public UserProfileImageDTO getImageById(Long imageId) {
        User user = getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);
        return mapToDTO(image);
    }
}
