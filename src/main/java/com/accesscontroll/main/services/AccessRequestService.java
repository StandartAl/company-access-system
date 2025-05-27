package com.accesscontroll.main.services;

import com.accesscontroll.main.DTO.AccessRequestDTO;
import com.accesscontroll.main.DTO.RoleDTO;
import com.accesscontroll.main.entities.AccessRequest;
import com.accesscontroll.main.entities.ProtectedResource;
import com.accesscontroll.main.entities.UserResourceAccess;
import com.accesscontroll.main.repositories.AccessRequestRepository;
import com.accesscontroll.main.repositories.ProtectedResourceRepository;
import com.accesscontroll.main.repositories.UserResourceAccessRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccessRequestService {

    private final AccessRequestRepository repository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserResourceAccessRepository resourceAccessRepository;
    private final ProtectedResourceRepository protectedResourceRepository;

    public AccessRequestService(AccessRequestRepository repository, KeycloakAdminService keycloakAdminService, UserResourceAccessRepository resourceAccessRepository, ProtectedResourceRepository protectedResourceRepository) {
        this.repository = repository;
        this.keycloakAdminService = keycloakAdminService;
        this.resourceAccessRepository = resourceAccessRepository;
        this.protectedResourceRepository = protectedResourceRepository;
    }

    public AccessRequest createRequest(AccessRequestDTO dto) {
        ProtectedResource resource = protectedResourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Ресурс не найден"));
        AccessRequest request = new AccessRequest();
        request.setUsername(dto.getUsername());
        request.setResource(resource);
        request.setJustification(dto.getJustification());
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        return repository.save(request);
    }

    public List<AccessRequest> getPendingRequests() {
        return repository.findByStatus("NEW");
    }

    public AccessRequest approveRequest(Long id) {
        AccessRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setStatus("APPROVED");

        // Сохраняем факт предоставленного доступа
        UserResourceAccess access = new UserResourceAccess();
        access.setUsername(request.getUsername());
        access.setResource(request.getResource());
        access.setJustification(request.getJustification());
        access.setGrantedAt(LocalDateTime.now());

        resourceAccessRepository.save(access);

        return repository.save(request);
    }


    public AccessRequest rejectRequest(Long id) {
        AccessRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setStatus("REJECTED");
        return repository.save(request);
    }

    public AccessRequest grantAccess(Long id) {
        AccessRequest req = repository.findById(id).orElseThrow();
        if (!"APPROVED".equals(req.getStatus())) throw new IllegalStateException("Not approved");
        req.setStatus("GRANTED");
        return repository.save(req);
    }
}
