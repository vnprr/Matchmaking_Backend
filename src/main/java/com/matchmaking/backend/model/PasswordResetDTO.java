package com.matchmaking.backend.model;

import com.matchmaking.backend.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetDTO {
    @NotBlank(message = "Token jest wymagany")
    private String token;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, message = "Hasło musi mieć przynajmniej 8 znaków")
    @StrongPassword
    private String newPassword;
}