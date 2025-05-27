package com.accesscontroll.main.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String status;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "resource_id") // создаст внешний ключ в AccessRequest
    private ProtectedResource resource;
    private String justification;
}

