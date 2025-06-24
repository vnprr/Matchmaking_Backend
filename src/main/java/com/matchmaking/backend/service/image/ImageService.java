package com.matchmaking.backend.service.image;

import com.matchmaking.backend.exception.ResourceNotFoundException;
import com.matchmaking.backend.model.auth.User;
import com.matchmaking.backend.model.profile.UserProfile;
import com.matchmaking.backend.model.image.ImageCropDTO;
import com.matchmaking.backend.model.image.ImageVersionsDTO;
import com.matchmaking.backend.model.image.UserProfileImage;
import com.matchmaking.backend.model.image.UserProfileImageDTO;
import com.matchmaking.backend.model.image.UserProfileImageOrderDTO;
import com.matchmaking.backend.repository.UserProfileImageRepository;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    public UserProfileImageDTO getAvatarProfileImage(Long userProfileId) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileAndIsAvatarTrue(profile)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserProfileImageDTO> getProfileImages(Long userProfileId) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileImageDTO uploadImage(
            MultipartFile file
    ) throws IOException {
        User user = userService.getCurrentUser();
        UserProfile profile = user.getProfile();

        // sprawdź limit zdjęć
        List<UserProfileImage> existingImages = imageRepository.findByUserProfile(profile);
        if (existingImages.size() >= maxImagesPerUser) {
            throw new IllegalStateException("Osiągnięto maksymalną liczbę zdjęć (" + maxImagesPerUser + ")");
        }

        ImageVersionsDTO versions = cloudinaryService.uploadImage(file);

        // kolejnosc nowego zdjęcia
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
                .isAvatar(false)
                .build();

        return mapToDTO(imageRepository.save(image));
    }

    @Transactional
    public UserProfileImageDTO cropImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        User user = userService.getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

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

        image.setGalleryUrl(galleryUrl);
        image.setThumbnailUrl(thumbnailUrl);
        imageRepository.save(image);

        return mapToDTO(image);
    }

    @Transactional
    public UserProfileImageDTO setAvatarImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        if (!cropDTO.isSquare()) {
            throw new IllegalStateException("Kadrowanie dla avatara musi być kwadratowe (1:1)");
        }

        User user = userService.getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        String avatarUrl = cloudinaryService.cropToAvatar(
                image.getPublicId(),
                cropDTO.getX(), cropDTO.getY(),
                cropDTO.getWidth(), cropDTO.getHeight()
        );
        image.setAvatarUrl(avatarUrl);

        imageRepository.findByUserProfileAndIsAvatarTrue(user.getProfile())
                .ifPresent(old -> {
                    old.setAvatar(false);
                    imageRepository.save(old);
                });

        image.setAvatar(true);
        imageRepository.save(image);

        return mapToDTO(image);
    }

    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        User user = userService.getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        cloudinaryService.deleteImage(image.getPublicId());
        imageRepository.delete(image);
        reorderImages(user.getProfile());
    }

    @Transactional
    public void updateImagesOrder(UserProfileImageOrderDTO orderDTO) {
        User user = userService.getCurrentUser();
        UserProfile profile = user.getProfile();

        List<UserProfileImage> images = imageRepository.findByUserProfile(profile);
        Map<Long, UserProfileImage> imageMap = images.stream()
                .collect(Collectors.toMap(UserProfileImage::getId, img -> img));

        for (UserProfileImageOrderDTO.ImageOrderItem item : orderDTO.getImages()) {
            UserProfileImage img = imageMap.get(item.getId());
            if (img == null) {
                throw new ResourceNotFoundException("Zdjęcie o ID " + item.getId() + " nie istnieje lub nie należy do tego użytkownika");
            }
            img.setDisplayOrder(item.getDisplayOrder());
        }
        imageRepository.saveAll(imageMap.values());
    }

    @Transactional(readOnly = true)
    public UserProfileImageDTO getImageById(Long imageId) {
        User user = userService.getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);
        return mapToDTO(image);
    }

    private UserProfile getUserProfileById(Long profileId) {
        if (profileId == null) {
            return userService.getCurrentUser().getProfile();
        }
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil o ID " + profileId + " nie istnieje"));
    }

    private UserProfileImage getImageAndCheckPermission(Long imageId, User user) {
        UserProfileImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Zdjęcie o ID " + imageId + " nie istnieje"));
        if (!image.getUserProfile().getId().equals(user.getProfile().getId())) {
            throw new ResourceNotFoundException("Zdjęcie o ID " + imageId + " nie należy do tego użytkownika");
        }
        return image;
    }

    private void reorderImages(UserProfile profile) {
        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setDisplayOrder(i);
        }
        imageRepository.saveAll(images);
    }

    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
        // podstawowe mapowanie pól
        UserProfileImageDTO dto = UserProfileImageDTO.builder()
                .id(image.getId())
                .originalUrl(image.getOriginalUrl())
                .galleryUrl(image.getGalleryUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .avatarUrl(image.getAvatarUrl())
                .isAvatar(image.isAvatar())
                .displayOrder(image.getDisplayOrder())
                .build();

        // dodatkowe informacje o wymiarach
        if (image.getPublicId() != null) {
            try {
                Map<String,Integer> dim = cloudinaryService.getImageDimensions(image.getPublicId());
                int origW = dim.get("width");
                int origH = dim.get("height");
                dto.setOriginalWidth(origW);
                dto.setOriginalHeight(origH);

                dto.setGalleryWidth(CloudinaryService.GALLERY_WIDTH);
                dto.setGalleryHeight(
                        (int)((long)origH * CloudinaryService.GALLERY_WIDTH / origW)
                );

                // miniaturka: kwadrat (jeśli ustawiona)
                dto.setThumbnailWidth(CloudinaryService.THUMBNAIL_SIZE);
                dto.setThumbnailHeight(CloudinaryService.THUMBNAIL_SIZE);

                // avatar: kwadrat (jeśli ustawiona)
                if (image.isAvatar()) {
                    dto.setAvatarWidth(CloudinaryService.AVATAR_SIZE);
                    dto.setAvatarHeight(CloudinaryService.AVATAR_SIZE);
                }
            } catch (Exception e) {

            }
        }

        return dto;
    }
}
