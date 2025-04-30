// src/main/java/com/matchmaking/backend/service/user/profile/image/CloudinaryService.java
package com.matchmaking.backend.service.user.profile.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Upload z optymalizacją (max 1024x1024, kompresja)
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
            "transformation", new Object[] {
                ObjectUtils.asMap(
                    "width", 1024,
                    "height", 1024,
                    "crop", "limit",
                    "quality", "auto:good",
                    "fetch_format", "auto"
                )
            }
        );
        return cloudinary.uploader().upload(file.getBytes(), options);
    }

    // Kadrowanie i resize do 1:1, 512x512
    public Map<String, Object> cropAndOptimizeImage(String publicId, int x, int y, int width, int height, int size) throws IOException {
        Map<String, Object> transformation = ObjectUtils.asMap(
            "transformation", new Object[] {
                ObjectUtils.asMap(
                    "crop", "crop",
                    "x", x,
                    "y", y,
                    "width", width,
                    "height", height,
                    "gravity", "custom"
                ),
                ObjectUtils.asMap(
                    "width", size,
                    "height", size,
                    "crop", "fill",
                    "quality", "auto:good",
                    "fetch_format", "auto"
                )
            }
        );
        return cloudinary.uploader().explicit(publicId, transformation);
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}


//package com.matchmaking.backend.service.user.profile.image;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class CloudinaryService {
//
//    private final Cloudinary cloudinary;
//
//    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
//        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//    }
//
//    public void deleteImage(String publicId) throws IOException {
//        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
//    }
//
//    public Map<String, Object> transformImage(String publicId, Map<String, Object> transformations) throws IOException {
//        Map<String, Object> options = ObjectUtils.asMap(
//                "transformation", transformations
//        );
//        return cloudinary.uploader().explicit(publicId, options);
//    }
//}