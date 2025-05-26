package com.matchmaking.backend.model;

import com.matchmaking.backend.model.Provider;
import com.matchmaking.backend.model.Role;
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