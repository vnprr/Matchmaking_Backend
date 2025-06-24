package com.matchmaking.backend.model.auth;

import com.matchmaking.backend.validation.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Email jest wymagany!")
    @Email(message = "Nieprawidłowy format adresu email!")
    private String email;

    @NotBlank(message = "Hasło jest wymagana!")
    @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków!")
    @StrongPassword
    private String password;
}
