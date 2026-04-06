package com.hms.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final UUID userId;
    private final UUID tenantId;
    private final Set<String> roles;
    private final Set<String> permissions;

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String module, String action) {
        return permissions.contains(module + ":" + action) || permissions.contains(module + "." + action);
    }
}
