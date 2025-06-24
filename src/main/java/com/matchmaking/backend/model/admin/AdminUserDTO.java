package com.matchmaking.backend.model.admin;

import com.matchmaking.backend.model.profile.Gender;
import com.matchmaking.backend.model.auth.Role;
import com.matchmaking.backend.model.auth.Provider;
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

    // dane profilu:
    private Long profileId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String bio;
}