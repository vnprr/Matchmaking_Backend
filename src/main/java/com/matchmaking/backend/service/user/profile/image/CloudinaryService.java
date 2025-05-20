package com.matchmaking.backend.service.user.profile.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.user.profile.image.ImageVersionsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String FOLDER = "user_profiles";
    private static final int THUMBNAIL_SIZE = 200;
    private static final int GALLERY_WIDTH = 1024;
    private static final int PROFILE_SIZE = 512;

    /**
     * Wgrywa zdjęcie i tworzy wszystkie potrzebne wersje
     */
    public ImageVersionsDTO uploadImage(MultipartFile file) throws IOException {
        String uniqueId = UUID.randomUUID().toString();
        String publicId = FOLDER + "/" + uniqueId;

        // Wgraj oryginalny plik
        Map<String, Object> originalResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("public_id", publicId));

        // Stwórz wersję galerii
        Map<String, Object> galleryResult = cloudinary.uploader().explicit(
                publicId,
                ObjectUtils.asMap(
                        "type", "upload", // Dodano "type"
                        "transformation", ObjectUtils.asMap(
                                "width", GALLERY_WIDTH,
                                "crop", "limit",
                                "quality", "auto:good",
                                "fetch_format", "auto"
                        )
                )
        );


        // Stwórz miniaturę
        Map<String, Object> thumbnailResult = cloudinary.uploader().explicit(
                publicId,
                ObjectUtils.asMap(
                        "type", "upload", // Dodano "type"
                        "transformation", ObjectUtils.asMap(
                                "width", THUMBNAIL_SIZE,
                                "height", THUMBNAIL_SIZE,
                                "crop", "fill",
                                "quality", "auto:good",
                                "fetch_format", "auto"
                        )
                )
        );

        return new ImageVersionsDTO(
                publicId,
                (String) originalResult.get("url"),
                (String) galleryResult.get("url"),
                (String) thumbnailResult.get("url")
        );
    }


    /**
     * Kadruje zdjęcie do określonego rozmiaru kwadratu
     */
    public String cropToProfile(String publicId, int x, int y, int width, int height) throws IOException {
        Map<String, Object> result = cloudinary.uploader().explicit(
                publicId,
                ObjectUtils.asMap(
                        "type", "upload", // Dodano "type"
                        "transformation", new Object[] {
                                ObjectUtils.asMap(
                                        "x", x,
                                        "y", y,
                                        "width", width,
                                        "height", height,
                                        "crop", "crop"
                                ),
                                ObjectUtils.asMap(
                                        "width", PROFILE_SIZE,
                                        "height", PROFILE_SIZE,
                                        "crop", "fill",
                                        "quality", "auto:good",
                                        "fetch_format", "auto"
                                )
                        }
                )
        );

        return (String) result.get("url");
    }


    /**
     * Usuwa zdjęcie z Cloudinary
     */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

}