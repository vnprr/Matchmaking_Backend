package com.matchmaking.backend.repository;

import com.matchmaking.backend.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}