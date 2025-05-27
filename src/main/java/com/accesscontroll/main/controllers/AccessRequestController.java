package com.accesscontroll.main.controllers;

import com.accesscontroll.main.DTO.AccessRequestDTO;
import com.accesscontroll.main.entities.AccessRequest;
import com.accesscontroll.main.repositories.AccessRequestRepository;
import com.accesscontroll.main.services.AccessRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {

    private final AccessRequestService service;
    private final AccessRequestRepository repository;

    public AccessRequestController(AccessRequestService service, AccessRequestRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<AccessRequest> getAllRequests() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<AccessRequest> createRequest(@RequestBody AccessRequestDTO requestDTO) {
        AccessRequest request = service.createRequest(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('manager') or hasRole('admin')")
    public List<AccessRequest> getPendingRequests() {
        return service.getPendingRequests();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('manager') or hasRole('admin')")
    public AccessRequest approve(@PathVariable Long id) {
        return service.approveRequest(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('manager') or hasRole('admin')")
    public AccessRequest reject(@PathVariable Long id) {
        return service.rejectRequest(id);
    }

    @PostMapping("/{id}/grant")
    @PreAuthorize("hasRole('admin')")
    public AccessRequest grantAccess(@PathVariable Long id) {
        return service.grantAccess(id);
    }
}
