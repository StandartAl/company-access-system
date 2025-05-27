package com.accesscontroll.main.controllers;

import com.accesscontroll.main.DTO.RoleDTO;
import com.accesscontroll.main.entities.UserResourceAccess;
import com.accesscontroll.main.repositories.UserResourceAccessRepository;
import com.accesscontroll.main.services.KeycloakAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminProxyController {

    private final KeycloakAdminService keycloakAdminService;
    private final UserResourceAccessRepository accessRepository;

    @Autowired
    public AdminProxyController(KeycloakAdminService keycloakAdminService,
                                UserResourceAccessRepository accessRepository) {
        this.keycloakAdminService = keycloakAdminService;
        this.accessRepository = accessRepository;
    }

    // ========== USERS ==========
    @GetMapping("/users")
    public List<?> getUsers() {
        return keycloakAdminService.getAllUsers();
    }

    @PutMapping("/users/{userId}")
    public void updateUser(@PathVariable String userId, @RequestBody Map<String, Object> updateData) {
        keycloakAdminService.updateUser(userId, updateData);
    }

    // ========== ROLES ==========
    @GetMapping("/roles")
    public List<Map<String, Object>> getRoles() {
        return keycloakAdminService.getRealmRoles();
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<String> assignRoleToUser(
            @PathVariable String userId,
            @RequestBody RoleDTO roleDTO) {
        try {
            keycloakAdminService.assignRealmRole(userId, roleDTO);
            return ResponseEntity.ok("Роль назначена");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при назначении роли: " + e.getMessage());
        }
    }

    // ========== ACCESS MANAGEMENT ==========

    // Получить все выданные доступы
    @GetMapping("/access")
    public List<UserResourceAccess> getAllAccesses() {
        return accessRepository.findAll();
    }

    // Отозвать доступ по ID
    @DeleteMapping("/access/{id}")
    public ResponseEntity<Void> revokeAccess(@PathVariable Long id) {
        if (!accessRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        accessRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Получить все доступы пользователя по username (необязательно)
    @GetMapping("/access/user/{username}")
    public List<UserResourceAccess> getAccessesByUser(@PathVariable String username) {
        return accessRepository.findByUsername(username);
    }
}
