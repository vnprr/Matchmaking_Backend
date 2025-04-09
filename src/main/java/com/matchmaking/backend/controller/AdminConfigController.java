package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.AppConfig;
import com.matchmaking.backend.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminConfigController {

    private final AppConfigService appConfigService;

    @GetMapping
    public ResponseEntity<List<AppConfig>> getAllConfigs() {
        return ResponseEntity.ok(appConfigService.getAllConfigs());
    }

    @GetMapping("/{key}")
    public ResponseEntity<AppConfig> getConfigByKey(@PathVariable String key) {
        return ResponseEntity.ok(appConfigService.getConfigByKey(key));
    }

    @PutMapping("/{key}")
    public ResponseEntity<AppConfig> updateConfig(@PathVariable String key, @RequestBody AppConfig config) {
        return ResponseEntity.ok(appConfigService.updateConfig(key, config));
    }
}