package com.matchmaking.backend.service.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.image.ImageVersionsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String FOLDER = "user_profiles";
    public  static final int ORIGINAL_MAX_SIZE = 2048;
    public  static final int GALLERY_WIDTH = 1024;
    public  static final int THUMBNAIL_SIZE = 200;
    public  static final int AVATAR_SIZE = 512;

    /**
     * Wgrywa oryginał (limit) + eager‐transformacje: gallery (limit) + thumbnail (fill).
     */
    public ImageVersionsDTO uploadImage(MultipartFile file) throws IOException {
        String uid      = UUID.randomUUID().toString();
        String publicId = FOLDER + "/" + uid;

        // 1) ogranicz oryginał
        Transformation originalTrans = new Transformation()
                .width(ORIGINAL_MAX_SIZE).height(ORIGINAL_MAX_SIZE)
                .crop("limit")
                .quality("auto:good");

        // 2) eager–galeria: max szerokość, proporcje zachowane
        Transformation galleryTrans = new Transformation()
                .width(GALLERY_WIDTH).crop("limit")
                .quality("auto:good")
                .fetchFormat("auto");

        // 3) eager–thumb: dokładny kwadrat
        Transformation thumbTrans = new Transformation()
                .width(THUMBNAIL_SIZE).height(THUMBNAIL_SIZE)
                .crop("fill")
                .quality("auto:good")
                .fetchFormat("auto");

        Map<String,Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "transformation", originalTrans,
                        "eager", Arrays.asList(galleryTrans, thumbTrans),
                        "eager_async", false  // synchronicznie zwracamy URL-e
                )
        );

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> eager = (List<Map<String,Object>>) result.get("eager");
        if (eager.size() < 2) {
            throw new IllegalStateException("Cloudinary: brak eager wyników.");
        }

        return new ImageVersionsDTO(
                publicId,
                (String) result.get("url"),            // oryginał
                (String) eager.get(0).get("url"),      // gallery
                (String) eager.get(1).get("url")       // thumbnail
        );
    }

    /**
     * Kadruje wg dokładnych współrzędnych, potem scala proporcjonalnie do szerokości galerii.
     */
    public String cropToGallery(String publicId, int x, int y, int w, int h) {
        Transformation t = new Transformation()
                .x(x).y(y).width(w).height(h).crop("crop")
                .chain()
                .width(GALLERY_WIDTH).crop("limit")
                .quality("auto:good")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(t)
                .generate(publicId);
    }

    /**
     * Kadruje wg współrzędnych, potem scala proporcjonalnie, by nie przekroczyć THUMBNAIL_SIZE w każdym wymiarze.
     */
    public String cropToThumbnail(String publicId, int x, int y, int w, int h) {
        Transformation t = new Transformation()
                .x(x).y(y).width(w).height(h).crop("crop")
                .chain()
                .width(THUMBNAIL_SIZE).height(THUMBNAIL_SIZE).crop("limit")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(t)
                .generate(publicId);
    }

    /**
     * Kadruje wg współrzędnych, a następnie przeskalowuje do dokładnego kwadratu AVATAR_SIZE×AVATAR_SIZE.
     */
    public String cropToAvatar(String publicId, int x, int y, int w, int h) {
        Transformation t = new Transformation()
                .x(x).y(y).width(w).height(h).crop("crop")
                .chain()
                .width(AVATAR_SIZE).height(AVATAR_SIZE).crop("scale")
                .quality("auto:good")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(t)
                .generate(publicId);
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("invalidate", true)
        );
    }

    /** Pobiera aktualne wymiary z zasobu Cloudinary. */
    public Map<String,Integer> getImageDimensions(String publicId) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String,Object> info = cloudinary.api()
                .resource(publicId, ObjectUtils.emptyMap());
        return Map.of(
                "width", (Integer) info.get("width"),
                "height", (Integer) info.get("height")
        );
    }
}
