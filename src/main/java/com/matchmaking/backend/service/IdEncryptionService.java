package com.matchmaking.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Base64;

@Service
public class IdEncryptionService {

    @Value("${app.id.encryption.key}")
    private String encryptionKey;

    /**
     * Szyfruje prywatne ID do publicznego ID.
     * @param id prywatne ID
     * @return publiczne ID
     */
    public String encryptId(Long id) {
        if (id == null) return null;

        try {
            // Przygotowanie klucza
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), "AES");

            // Inicjalizacja szyfru
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Konwersja Long na ByteBuffer
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(id);

            // Szyfrowanie
            byte[] encryptedBytes = cipher.doFinal(buffer.array());

            // Konwersja do Base64 i usunięcie znaków specjalnych
            return Base64.getUrlEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("ID encryption error!", e);
        }
    }

    /**
     * Deszyfruje publiczne ID do prywatnego ID.
     * @param publicId publiczne ID
     * @return prywatne ID
     */
    public Long decryptId(String publicId) {
        if (publicId == null || publicId.isEmpty()) return null;

        try {
            // Przygotowanie klucza
            SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(), "AES");

            // Inicjalizacja szyfru
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Deszyfrowanie
            byte[] decodedBytes = Base64.getUrlDecoder().decode(publicId);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            // Konwersja z ByteBuffer do Long
            ByteBuffer buffer = ByteBuffer.wrap(decryptedBytes);
            return buffer.getLong();
        } catch (Exception e) {
            // Zwracamy null w przypadku błędu, aby obsłużyć nieprawidłowe parametry
            return null;
        }
    }

    /**
     * Zwraca bajty klucza szyfrowania. <p>
     * Klucz musi mieć dokładnie 16, 24 lub 32 bajtów dla AES <i>(Advanced Encryption Standard)</i>. <p>
     * @return bajty klucza
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes = encryptionKey.getBytes();
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            byte[] adjustedKey = new byte[16]; // Domyślnie 16 bajtów (128 bitów)
            System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, 16));
            return adjustedKey;
        }
        return keyBytes;
    }
}