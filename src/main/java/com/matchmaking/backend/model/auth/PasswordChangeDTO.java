package com.matchmaking.backend.model.auth;

import com.matchmaking.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDTO {
    @NotBlank(message = "Aktualne hasło jest wymagane!")
    private String currentPassword;

    @NotBlank(message = "Nowe hasło jest wymagane!")
    @Size(min = 8, message = "Nowe hasło musi mieć co najmniej 8 znaków!")
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Potwierdzenie nowego hasła jest wymagane!")
    private String confirmNewPassword;
}