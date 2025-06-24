package com.matchmaking.backend.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {
    @NotBlank(message = "Adres email jest wymagany!")
    @Email(message = "Podaj poprawny adres email!")
    private String email;
}