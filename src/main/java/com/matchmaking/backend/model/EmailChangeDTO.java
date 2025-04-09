package com.matchmaking.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeDTO {
    @NotBlank(message = "Hasło jest wymagane")
    private String password;

    @NotBlank(message = "Nowy adres email jest wymagany")
    @Email(message = "Niepoprawny format adresu email")
    private String newEmail;
}