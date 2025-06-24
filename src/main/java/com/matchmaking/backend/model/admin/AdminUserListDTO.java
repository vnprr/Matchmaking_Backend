package com.matchmaking.backend.model.admin;

import com.matchmaking.backend.model.auth.Role;
import com.matchmaking.backend.model.auth.Provider;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListDTO {
    private Long id;
    private String email;
    private Long profileId;
    private String firstName;
    private String lastName;
    private Role role;
    private Provider provider;
    private boolean enabled;
    private LocalDateTime createdAt;
}