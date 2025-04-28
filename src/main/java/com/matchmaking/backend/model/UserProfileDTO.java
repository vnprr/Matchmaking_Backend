package com.matchmaking.backend.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDTO {
    //private Long userId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String bio;
    // private String profileImage;
}