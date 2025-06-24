package com.matchmaking.backend.model.auth;

import com.matchmaking.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetDTO {
    @NotBlank(message = "Token jest wymagany!")
    private String token;

    @NotBlank(message = "Nowe hasło jest wymagane!")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków!")
    @StrongPassword
    private String newPassword;
}