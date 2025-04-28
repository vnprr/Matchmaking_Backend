package com.matchmaking.backend.model.user.profile;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileContextDTO {
    private Long userId;
    private boolean editable;
    private boolean viewable;
}