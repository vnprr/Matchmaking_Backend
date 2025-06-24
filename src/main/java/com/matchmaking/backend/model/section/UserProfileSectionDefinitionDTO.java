package com.matchmaking.backend.model.section;

import lombok.Data;

@Data
public class UserProfileSectionDefinitionDTO {
    private Long id;
    private String name;
    private int displayOrder;
    private boolean required;
    private boolean visible;
    private String description;
}