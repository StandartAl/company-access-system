package com.accesscontroll.main.DTO;

import lombok.Data;

@Data
public class AccessRequestDTO {
    private String username;
    private Long resourceId;
    private String justification;
}
