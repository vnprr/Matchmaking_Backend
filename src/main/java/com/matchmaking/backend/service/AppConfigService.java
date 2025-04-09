package com.matchmaking.backend.service;

import com.matchmaking.backend.model.AppConfig;
import com.matchmaking.backend.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;

    @Transactional(readOnly = true)
    public List<AppConfig> getAllConfigs() {
        return appConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppConfig getConfigByKey(String key) {
        return appConfigRepository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Konfiguracja o podanym kluczu nie istnieje"));
    }

    @Transactional
    public AppConfig updateConfig(String key, AppConfig config) {
        AppConfig existingConfig = appConfigRepository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Konfiguracja o podanym kluczu nie istnieje"));

        existingConfig.setParamValue(config.getParamValue());
        existingConfig.setDescription(config.getDescription());

        return appConfigRepository.save(existingConfig);
    }

    @Transactional(readOnly = true)
    public String getConfigValue(String key, String defaultValue) {
        return appConfigRepository.findById(key)
                .map(AppConfig::getParamValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public Integer getConfigValueAsInt(String key, Integer defaultValue) {
        String value = getConfigValue(key, null);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}