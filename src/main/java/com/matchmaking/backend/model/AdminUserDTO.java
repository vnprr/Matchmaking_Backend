package com.matchmaking.backend.model;

import com.matchmaking.backend.model.Gender;
import com.matchmaking.backend.model.Provider;
import com.matchmaking.backend.model.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Long id;
    private String email;
    private String newPassword;
    private boolean enabled;
    private Role role;
    private Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;

    // Dane profilu
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String bio;
}