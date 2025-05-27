package com.accesscontroll.main.controllers;

import com.accesscontroll.main.entities.ProtectedResource;
import com.accesscontroll.main.repositories.ProtectedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ProtectedResourceController {

    private final ProtectedResourceRepository resourceRepository;

    @GetMapping
    public List<ProtectedResource> getResources(Authentication authentication) {
        String username = authentication.getName();

        // Проверка: если у пользователя есть роль 'admin' — вернуть все ресурсы
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_admin"))) {
            return resourceRepository.findAll();
        }

        // Иначе — вернуть только те ресурсы, к которым есть доступ
        return resourceRepository.findResourcesByUsername(username);
    }

    @GetMapping("/available")
    public List<ProtectedResource> getAvailableResources(Authentication authentication) {
        String username = authentication.getName();

        // Получаем ресурсы, к которым уже есть доступ
        List<ProtectedResource> granted = resourceRepository.findResourcesByUsername(username);

        if (granted.isEmpty()) {
            return resourceRepository.findAll(); // можно запросить всё
        }

        return resourceRepository.findAll().stream()
                .filter(resource -> !granted.contains(resource))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ProtectedResource create(@RequestBody ProtectedResource resource) {
        return resourceRepository.save(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (resourceRepository.existsById(id)) {
            resourceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}