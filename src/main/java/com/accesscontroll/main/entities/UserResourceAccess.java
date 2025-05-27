package com.accesscontroll.main.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class UserResourceAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private ProtectedResource resource;

    private String permission;
    private String justification;

    private LocalDateTime grantedAt;
}
