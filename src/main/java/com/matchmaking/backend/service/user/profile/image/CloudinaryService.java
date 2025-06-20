package com.matchmaking.backend.service.user.profile.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.matchmaking.backend.model.user.profile.image.ImageVersionsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String FOLDER = "user_profiles";
    public static final int THUMBNAIL_SIZE = 200;
    public static final int GALLERY_WIDTH = 1024;
    public static final int AVATAR_SIZE = 512;
    public static final int ORIGINAL_MAX_SIZE = 2048;

    /**
     * Wgrywa zdjęcie i tworzy wszystkie potrzebne wersje
     */
    public ImageVersionsDTO uploadImage(MultipartFile file) throws IOException {
        String uniqueId = UUID.randomUUID().toString();
        String publicId = FOLDER + "/" + uniqueId;

        // Walidacja wymiarów
        if (ORIGINAL_MAX_SIZE <= 0) {
            throw new IllegalStateException("ORIGINAL_MAX_SIZE musi być większe od 0");
        }

        Transformation originalTrans = new Transformation()
            .width(ORIGINAL_MAX_SIZE).height(ORIGINAL_MAX_SIZE)
            .crop("limit").quality("auto:good");

        Transformation galleryTrans = new Transformation()
            .width(GALLERY_WIDTH).crop("limit")
            .quality("auto:good").fetchFormat("auto");

        Transformation thumbnailTrans = new Transformation()
            .width(THUMBNAIL_SIZE).height(THUMBNAIL_SIZE)
            .crop("fill").quality("auto:good").fetchFormat("auto");

        // Wgraj oryginalny plik z eager transformations dla wszystkich wersji
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
            ObjectUtils.asMap(
                "public_id", publicId,
                "transformation", originalTrans,
                "eager", Arrays.asList(galleryTrans, thumbnailTrans),
                "eager_async", false // upewnij się, że zwróci URL od razu
            ));

        // Weryfikacja wyników przetwarzania
        if (uploadResult.get("url") == null || uploadResult.get("eager") == null) {
            throw new IllegalStateException("Upload obrazu nie powiódł się.");
        }

        // Pobierz wyniki eager transformations
        List<Map<String, Object>> eagerResults = (List<Map<String, Object>>) uploadResult.get("eager");

        // Sprawdź czy mamy wszystkie potrzebne transformacje
        if (eagerResults.size() < 2) {
            throw new IllegalStateException("Nie wszystkie transformacje zostały wykonane.");
        }

        return new ImageVersionsDTO(
            publicId,
            (String) uploadResult.get("url"),
            (String) eagerResults.get(0).get("url"),
            (String) eagerResults.get(1).get("url")
        );
    }



    /**
     * Kadruje zdjęcie do określonego obszaru i zwraca URL wersji avatara (kwadrat).
     *
     * @param publicId identyfikator zdjęcia w Cloudinary
     * @param x        pozycja X początku kadrowania
     * @param y        pozycja Y początku kadrowania
     * @param width    szerokość kadrowanego obszaru
     * @param height   wysokość kadrowanego obszaru
     * @return URL przyciętego kwadratu o wymiarach AVATAR_SIZE×AVATAR_SIZE
     */
    public String cropToAvatar(String publicId, int x, int y, int width, int height) {
        Transformation avatarTrans = new Transformation()
                // najpierw precyzyjne kadrowanie wg parametrów użytkownika
                .x(x).y(y)
                .width(width).height(height).crop("crop")
                .chain()
                // następnie skalujemy tę wyciętą część do kwadratu
                .width(AVATAR_SIZE).height(AVATAR_SIZE).crop("scale")
                .quality("auto:good")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(avatarTrans)
                .generate(publicId);
    }

    /**
     * Kadruje zdjęcie do określonego obszaru i tworzy wersję galerii.
     * Zachowuje proporcje wyciętego obszaru, ale ogranicza szerokość do GALLERY_WIDTH.
     *
     * @param publicId identyfikator zdjęcia w Cloudinary
     * @param x pozycja X początku kadrowania (liczona od lewego górnego rogu)
     * @param y pozycja Y początku kadrowania (liczona od lewego górnego rogu)
     * @param width szerokość kadrowanego obszaru
     * @param height wysokość kadrowanego obszaru
     * @return URL przyciętego zdjęcia w wersji galerii
     */
    public String cropToGallery(String publicId, int x, int y, int width, int height) {
        Transformation galleryTrans = new Transformation()
                .x(x).y(y)
                .width(width).height(height).crop("crop")   // pierwszy etap – dokładne kadrowanie
                .chain()
                .width(GALLERY_WIDTH).crop("scale")          // drugi etap – skalowanie do szerokości galerii
                .quality("auto:good")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(galleryTrans)
                .generate(publicId);
    }

    /**
     * Kadruje zdjęcie do określonego obszaru i tworzy miniaturkę.
     *
     * @param publicId identyfikator zdjęcia w Cloudinary
     */
    public String cropToThumbnail(String publicId, int x, int y, int width, int height) {
        Transformation thumbTrans = new Transformation()
                // najpierw dokładne kadrowanie wg parametrów użytkownika
                .x(x).y(y)
                .width(width).height(height).crop("crop")
                .chain()
                // teraz skalowanie z zachowaniem proporcji — limit
                .width(THUMBNAIL_SIZE).height(THUMBNAIL_SIZE)
                .crop("limit")
                .quality("auto")
                .fetchFormat("auto");

        return cloudinary.url()
                .secure(true)
                .transformation(thumbTrans)
                .generate(publicId);
    }



    /**
     * Usuwa zdjęcie z Cloudinary
     */
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    /**
     * Pobiera wymiary obrazu z Cloudinary.
     *
     * @param publicId identyfikator zdjęcia w Cloudinary
     * @return mapa zawierająca szerokość i wysokość obrazu (klucze: "width", "height")
     * @throws IOException w przypadku błędu podczas komunikacji z Cloudinary
     * @throws Exception w przypadku innych błędów
     */
    public Map<String, Integer> getImageDimensions(String publicId) throws IOException, Exception {
        Map<String, Object> result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());

        Map<String, Integer> dimensions = new java.util.HashMap<>();
        dimensions.put("width", (Integer) result.get("width"));
        dimensions.put("height", (Integer) result.get("height"));

        return dimensions;
    }
}
