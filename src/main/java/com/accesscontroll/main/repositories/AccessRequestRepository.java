package com.accesscontroll.main.repositories;

import com.accesscontroll.main.entities.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByStatus(String status);
}
