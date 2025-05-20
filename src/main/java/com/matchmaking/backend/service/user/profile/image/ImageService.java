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
    public UserProfileImageDTO getMainProfileImage(Long userProfileId) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileAndIsMainTrue(profile)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<UserProfileImageDTO> getProfileImages(Long userProfileId) {
        UserProfile profile = getUserProfileById(userProfileId);
        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public UserProfileImageDTO uploadImage(MultipartFile file) throws IOException {
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
                .isMain(existingImages.isEmpty()) // Pierwsze zdjęcie staje się główne
                .build();

        return mapToDTO(imageRepository.save(image));
    }

    @Transactional
    public UserProfileImageDTO cropImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
        User user = getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        // Kadruj zdjęcie za pomocą Cloudinary
        String profileUrl = cloudinaryService.cropToProfile(
                image.getPublicId(),
                cropDTO.getX(),
                cropDTO.getY(),
                cropDTO.getWidth(),
                cropDTO.getHeight()
        );

        image.setProfileUrl(profileUrl);
        return mapToDTO(imageRepository.save(image));
    }

    @Transactional
    public UserProfileImageDTO setMainImage(Long imageId) throws IOException {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();
        UserProfileImage newMainImage = getImageAndCheckPermission(imageId, user);

        // Jeśli obrazek nie ma jeszcze przyciętej wersji profilowej, zwróć błąd
        if (newMainImage.getProfileUrl() == null) {
            throw new IllegalStateException("Zdjęcie musi być najpierw przycięte, aby ustawić je jako główne");
        }

        // Zmień status aktualnego głównego zdjęcia
        imageRepository.findByUserProfileAndIsMainTrue(profile)
                .ifPresent(currentMain -> {
                    currentMain.setMain(false);
                    imageRepository.save(currentMain);
                });

        // Ustaw nowe zdjęcie jako główne
        newMainImage.setMain(true);
        return mapToDTO(imageRepository.save(newMainImage));
    }

    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        User user = getCurrentUser();
        UserProfileImage image = getImageAndCheckPermission(imageId, user);

        // Usuń zdjęcie z Cloudinary
        cloudinaryService.deleteImage(image.getPublicId());

        // Sprawdź, czy to było główne zdjęcie
        boolean wasMain = image.isMain();

        // Usuń z bazy danych
        imageRepository.delete(image);

        // Jeśli było główne, ustaw inne jako główne
        if (wasMain) {
            UserProfile profile = user.getProfile();
            List<UserProfileImage> remainingImages = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
            if (!remainingImages.isEmpty()) {
                UserProfileImage newMain = remainingImages.get(0);
                // Sprawdź, czy ma przyciętą wersję profilową
                if (newMain.getProfileUrl() != null) {
                    newMain.setMain(true);
                    imageRepository.save(newMain);
                }
            }
        }

        // Aktualizuj kolejność pozostałych zdjęć
        reorderImages(user.getProfile());
    }

    @Transactional
    public void updateImagesOrder(UserProfileImageOrderDTO orderDTO) {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Pobierz wszystkie zdjęcia użytkownika
        List<UserProfileImage> userImages = imageRepository.findByUserProfile(profile);
        Map<Long, UserProfileImage> imageMap = userImages.stream()
                .collect(Collectors.toMap(UserProfileImage::getId, img -> img));

        // Aktualizuj kolejność
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

    private UserProfileImage getImageAndCheckPermission(Long imageId, User user) {
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
            // Jeśli profileId jest null, pobierz profil zalogowanego użytkownika
            return getCurrentUser().getProfile();
        }
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil o ID " + profileId + " nie istnieje"));
    }

    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
        return UserProfileImageDTO.builder()
                .id(image.getId())
                .originalUrl(image.getOriginalUrl())
                .galleryUrl(image.getGalleryUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .profileUrl(image.getProfileUrl())
                .isMain(image.isMain())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}



//package com.matchmaking.backend.service.user.profile.image;
//
//import com.matchmaking.backend.exception.ResourceNotFoundException;
//import com.matchmaking.backend.model.User;
//import com.matchmaking.backend.model.UserProfile;
//import com.matchmaking.backend.model.user.profile.image.*;
//import com.matchmaking.backend.repository.UserProfileImageRepository;
//import com.matchmaking.backend.repository.UserProfileRepository;
//import com.matchmaking.backend.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ImageService {
//
//    private final UserService userService;
//    private final UserProfileRepository userProfileRepository;
//    private final CloudinaryService cloudinaryService;
//    private final UserProfileImageRepository imageRepository;
//
//    @Value("${app.profile.max-images:10}")
//    private int maxImagesPerUser;
//
//    @Transactional(readOnly = true)
//    public UserProfileImageDTO getMainProfileImage(Long userProfileId) {
//        UserProfile profile = getUserProfileById(userProfileId);
//        return imageRepository.findByUserProfileAndIsMainTrue(profile)
//                .map(this::mapToDTO)
//                .orElse(null);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserProfileImageDTO> getProfileImages(Long userProfileId) {
//        UserProfile profile = getUserProfileById(userProfileId);
//        return imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile)
//                .stream()
//                .map(this::mapToDTO)
//                .toList();
//    }
//
//    @Transactional
//    public UserProfileImageDTO uploadImage(MultipartFile file) throws IOException {
//        User user = getCurrentUser();
//        UserProfile profile = user.getProfile();
//
//        // Sprawdź limit zdjęć
//        List<UserProfileImage> existingImages = imageRepository.findByUserProfile(profile);
//        if (existingImages.size() >= maxImagesPerUser) {
//            throw new IllegalStateException("Osiągnięto maksymalną liczbę zdjęć (" + maxImagesPerUser + ")");
//        }
//
//        // Wgraj wszystkie wersje zdjęcia do Cloudinary
//        CloudinaryService.ImageVersions versions = cloudinaryService.uploadImage(file);
//
//        // Ustal kolejność nowego zdjęcia
//        int displayOrder = existingImages.isEmpty() ? 0 :
//                existingImages.stream()
//                        .mapToInt(UserProfileImage::getDisplayOrder)
//                        .max()
//                        .orElse(-1) + 1;
//
//        // Zapisz w bazie danych
//        UserProfileImage image = UserProfileImage.builder()
//                .userProfile(profile)
//                .publicId(versions.getPublicId())
//                .originalUrl(versions.getOriginalUrl())
//                .galleryUrl(versions.getGalleryUrl())
//                .thumbnailUrl(versions.getThumbnailUrl())
//                .displayOrder(displayOrder)
//                .isMain(existingImages.isEmpty()) // Pierwsze zdjęcie staje się główne
//                .build();
//
//        return mapToDTO(imageRepository.save(image));
//    }
//
//    @Transactional
//    public UserProfileImageDTO cropImage(Long imageId, ImageCropDTO cropDTO) throws IOException {
//        User user = getCurrentUser();
//        UserProfileImage image = getImageAndCheckPermission(imageId, user);
//
//        // Kadruj zdjęcie za pomocą Cloudinary
//        String profileUrl = cloudinaryService.cropToProfile(
//                image.getPublicId(),
//                cropDTO.getX(),
//                cropDTO.getY(),
//                cropDTO.getWidth(),
//                cropDTO.getHeight()
//        );
//
//        image.setProfileUrl(profileUrl);
//        return mapToDTO(imageRepository.save(image));
//    }
//
//    @Transactional
//    public UserProfileImageDTO setMainImage(Long imageId) throws IOException {
//        User user = getCurrentUser();
//        UserProfile profile = user.getProfile();
//        UserProfileImage newMainImage = getImageAndCheckPermission(imageId, user);
//
//        // Jeśli obrazek nie ma jeszcze przyciętej wersji profilowej, zwróć błąd
//        if (newMainImage.getProfileUrl() == null) {
//            throw new IllegalStateException("Zdjęcie musi być najpierw przycięte, aby ustawić je jako główne");
//        }
//
//        // Zmień status aktualnego głównego zdjęcia
//        imageRepository.findByUserProfileAndIsMainTrue(profile)
//                .ifPresent(currentMain -> {
//                    currentMain.setMain(false);
//                    imageRepository.save(currentMain);
//                });
//
//        // Ustaw nowe zdjęcie jako główne
//        newMainImage.setMain(true);
//        return mapToDTO(imageRepository.save(newMainImage));
//    }
//
//    @Transactional
//    public void deleteImage(Long imageId) throws IOException {
//        User user = getCurrentUser();
//        UserProfileImage image = getImageAndCheckPermission(imageId, user);
//
//        // Usuń zdjęcie z Cloudinary
//        cloudinaryService.deleteImage(image.getPublicId());
//
//        // Sprawdź czy to było główne zdjęcie
//        boolean wasMain = image.isMain();
//
//        // Usuń z bazy danych
//        imageRepository.delete(image);
//
//        // Jeśli było główne, ustaw inne jako główne
//        if (wasMain) {
//            UserProfile profile = user.getProfile();
//            List<UserProfileImage> remainingImages = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
//            if (!remainingImages.isEmpty()) {
//                UserProfileImage newMain = remainingImages.get(0);
//                // Sprawdź czy ma przyciętą wersję profilową
//                if (newMain.getProfileUrl() != null) {
//                    newMain.setMain(true);
//                    imageRepository.save(newMain);
//                }
//            }
//        }
//
//        // Aktualizuj kolejność pozostałych zdjęć
//        reorderImages(user.getProfile());
//    }
//
//    @Transactional
//    public void updateImagesOrder(UserProfileImageOrderDTO orderDTO) {
//        User user = getCurrentUser();
//        UserProfile profile = user.getProfile();
//
//        // Pobierz wszystkie zdjęcia użytkownika
//        List<UserProfileImage> userImages = imageRepository.findByUserProfile(profile);
//        Map<Long, UserProfileImage> imageMap = userImages.stream()
//                .collect(Collectors.toMap(UserProfileImage::getId, img -> img));
//
//        // Aktualizuj kolejność
//        for (UserProfileImageOrderDTO.ImageOrderItem item : orderDTO.getImages()) {
//            UserProfileImage image = imageMap.get(item.getId());
//            if (image != null) {
//                image.setDisplayOrder(item.getDisplayOrder());
//            } else {
//                throw new ResourceNotFoundException("Zdjęcie o ID " + item.getId() + " nie istnieje lub nie należy do tego użytkownika");
//            }
//        }
//
//        imageRepository.saveAll(imageMap.values());
//    }
//
//    private void reorderImages(UserProfile profile) {
//        List<UserProfileImage> images = imageRepository.findByUserProfileOrderByDisplayOrderAsc(profile);
//        for (int i = 0; i < images.size(); i++) {
//            UserProfileImage img = images.get(i);
//            img.setDisplayOrder(i);
//        }
//        imageRepository.saveAll(images);
//    }
//
//    private UserProfileImage getImageAndCheckPermission(Long imageId, User user) {
//        UserProfileImage image = imageRepository.findById(imageId)
//                .orElseThrow(() -> new ResourceNotFoundException("Zdjęcie o ID " + imageId + " nie istnieje"));
//
//        if (!image.getUserProfile().getId().equals(user.getProfile().getId())) {
//            throw new IllegalArgumentException("Brak uprawnień do tego zdjęcia");
//        }
//
//        return image;
//    }
//
//    private User getCurrentUser() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        return userService.getUserByEmail(email);
//    }
//
//    private UserProfile getUserProfileById(Long profileId) {
//        return userProfileRepository.findById(profileId)
//                .orElseThrow(() -> new ResourceNotFoundException("Profil o ID " + profileId + " nie istnieje"));
//    }
//
//    private UserProfileImageDTO mapToDTO(UserProfileImage image) {
//        return UserProfileImageDTO.builder()
//                .id(image.getId())
//                .originalUrl(image.getOriginalUrl())
//                .galleryUrl(image.getGalleryUrl())
//                .thumbnailUrl(image.getThumbnailUrl())
//                .profileUrl(image.getProfileUrl())
//                .isMain(image.isMain())
//                .displayOrder(image.getDisplayOrder())
//                .build();
//    }
//}