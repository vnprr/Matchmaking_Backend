package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.section.UserProfileSectionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfileSectionDefinitionRepository extends JpaRepository<UserProfileSectionDefinition, Long> {
    List<UserProfileSectionDefinition> findAllByVisibleTrueOrderByDisplayOrderAsc();
    List<UserProfileSectionDefinition> findAllByOrderByDisplayOrderAsc();
}