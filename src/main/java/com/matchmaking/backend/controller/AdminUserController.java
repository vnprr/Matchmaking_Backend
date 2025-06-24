package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.auth.Role;
import com.matchmaking.backend.model.admin.AdminUserDTO;
import com.matchmaking.backend.model.admin.AdminUserListDTO;
import com.matchmaking.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Kontroler do zarządzania użytkownikami przez administratora.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    /**
     * Pobiera listę użytkowników z opcjonalnym filtrowaniem i paginacją.
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserListDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(adminService.getUsers(page, size, search));
    }

    /**
     * Pobiera szczegóły użytkownika po ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    /**
     * Tworzy nowego użytkownika.
     */
    @PostMapping
    public ResponseEntity<AdminUserDTO> createUser(@RequestBody AdminUserDTO userDTO) {
        return ResponseEntity.ok(adminService.createUser(userDTO));
    }

    /**
     * Aktualizuje istniejącego użytkownika.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDTO> updateUser(@PathVariable Long id, @RequestBody AdminUserDTO userDTO) {
        return ResponseEntity.ok(adminService.updateUser(id, userDTO));
    }

    /**
     * Usuwa użytkownika po ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Zmienia rolę użytkownika.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<Void> changeUserRole(@PathVariable Long id, @RequestBody Role role) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    /**
     * Włącza lub wyłącza użytkownika.
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        adminService.changeUserStatus(id, true);
        return ResponseEntity.ok().build();
    }

    /**
     * Wyłącza użytkownika.
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        adminService.changeUserStatus(id, false);
        return ResponseEntity.ok().build();
    }
}