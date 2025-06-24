package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.AppConfig;
import com.matchmaking.backend.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler do zarządzania konfiguracją aplikacji przez administratora.
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminConfigController {

    private final AppConfigService appConfigService;

    /**
     * Pobiera wszystkie konfiguracje aplikacji.
     *
     * @return Lista konfiguracji aplikacji.
     */
    @GetMapping
    public ResponseEntity<List<AppConfig>> getAllConfigs() {
        return ResponseEntity.ok(appConfigService.getAllConfigs());
    }

    /**
     * Pobiera konfigurację aplikacji po kluczu.
     *
     * @param key Klucz konfiguracji.
     * @return Konfiguracja aplikacji.
     */
    @GetMapping("/{key}")
    public ResponseEntity<AppConfig> getConfigByKey(@PathVariable String key) {
        return ResponseEntity.ok(appConfigService.getConfigByKey(key));
    }

    /**
     * Tworzy nową konfigurację aplikacji.
     *
     * @param config Konfiguracja aplikacji do utworzenia.
     * @return Utworzona konfiguracja aplikacji.
     */
    @PutMapping("/{key}")
    public ResponseEntity<AppConfig> updateConfig(
            @PathVariable String key,
            @RequestBody AppConfig config
    ) {
        return ResponseEntity.ok(appConfigService.updateConfig(key, config));
    }
}