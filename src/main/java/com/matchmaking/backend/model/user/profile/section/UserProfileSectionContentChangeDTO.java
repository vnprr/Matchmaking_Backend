package com.matchmaking.backend.model.user.profile.section;

import lombok.Data;

@Data
public class UserProfileSectionContentChangeDTO {
    private Long sectionId;
    private String content;
}
