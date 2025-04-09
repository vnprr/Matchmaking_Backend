package com.matchmaking.backend.controller;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.AdminUserDTO;
import com.matchmaking.backend.model.AdminUserListDTO;
import com.matchmaking.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<AdminUserListDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<AdminUserDTO> createUser(@RequestBody AdminUserDTO userDTO) {
        return ResponseEntity.ok(adminService.createUser(userDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDTO> updateUser(@PathVariable Long id, @RequestBody AdminUserDTO userDTO) {
        return ResponseEntity.ok(adminService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> changeUserRole(@PathVariable Long id, @RequestBody Role role) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        adminService.changeUserStatus(id, true);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        adminService.changeUserStatus(id, false);
        return ResponseEntity.ok().build();
    }
}