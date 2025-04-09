package com.matchmaking.backend.model;

import com.matchmaking.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDTO {
    @NotBlank(message = "Obecne hasło jest wymagane")
    private String currentPassword;

    @NotBlank(message = "Nowe hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć przynajmniej 8 znaków")
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Powtórzenie hasła jest wymagane")
    private String confirmNewPassword;
}