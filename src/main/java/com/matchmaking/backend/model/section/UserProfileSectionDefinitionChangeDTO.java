package com.matchmaking.backend.model.section;
import lombok.Data;

@Data
public class UserProfileSectionDefinitionChangeDTO {
    private String name;
    private boolean required;
    private boolean visible;
    private String description;
}
