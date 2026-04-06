package com.hms.repository;

import com.hms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findBySlugAndTenantIdAndDeletedAtIsNull(String slug, UUID tenantId);
    List<Role> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
