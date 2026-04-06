package com.hms.dto.auth;

import lombok.*;
import java.util.Set;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UUID tenantId;
    private String tenantName;
    private Set<String> roles;
    private Set<String> permissions;
}
