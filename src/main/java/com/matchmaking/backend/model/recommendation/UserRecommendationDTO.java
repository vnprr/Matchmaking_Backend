package com.matchmaking.backend.model.recommendation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRecommendationDTO {
    private Long id;
    private Long recommendedProfileId;
    private String recommendedProfileName;
    private RecommendationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime viewedAt;
    private Long createdById;
}
