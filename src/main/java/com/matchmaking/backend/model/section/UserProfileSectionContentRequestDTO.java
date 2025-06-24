package com.matchmaking.backend.model.section;

import lombok.Data;

@Data
public class UserProfileSectionContentRequestDTO {
    private Long sectionId;
    private String sectionName;
    private String content;
    private Boolean required;
}