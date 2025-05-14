package com.matchmaking.backend.mapper;

import com.matchmaking.backend.model.recommendation.UserRecommendation;
import com.matchmaking.backend.model.recommendation.UserRecommendationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRecommendationMapper {

    UserRecommendationDTO toDto(UserRecommendation entity);

    UserRecommendation toEntity(UserRecommendationDTO dto);
}