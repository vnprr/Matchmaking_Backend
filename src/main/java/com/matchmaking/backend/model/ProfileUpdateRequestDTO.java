package com.matchmaking.backend.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDTO {

    private Long id;

    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 2, max = 50, message = "Imię powinno mieć od 2 do 50 znaków")
    @Pattern(regexp = "^(?!.*[0-9])[\\p{L}\\p{M}\\s'.-]+$", message = "Imię może zawierać tylko litery, spacje, apostrofy, myślniki oraz kropki, bez cyfr.")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 2, max = 50, message = "Nazwisko powinno mieć od 2 do 50 znaków")
    @Pattern(regexp = "^(?!.*[0-9])[\\p{L}\\p{M}\\s'.-]+$", message = "Nazwisko może zawierać tylko litery, spacje, apostrofy, myślniki oraz kropki, bez cyfr.")
    private String lastName;

    @NotNull(message = "Płeć musi być podana")
    private Gender gender;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być z przeszłości")
    private LocalDate dateOfBirth;
}