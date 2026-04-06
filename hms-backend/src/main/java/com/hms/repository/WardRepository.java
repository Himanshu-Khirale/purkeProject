package com.hms.repository;

import com.hms.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID> {
    List<Ward> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<Ward> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
