package com.matchmaking.backend.model.profile;

import lombok.Data;

@Data
public class UserProfileContextDTO {
    private Long profileId;
    private boolean editable;
    private boolean viewable;
    private boolean owner;
}

//private Long userId;
