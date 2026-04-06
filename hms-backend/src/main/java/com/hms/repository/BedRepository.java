package com.hms.repository;

import com.hms.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BedRepository extends JpaRepository<Bed, UUID> {
    List<Bed> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    List<Bed> findByWardIdAndTenantIdAndDeletedAtIsNull(UUID wardId, UUID tenantId);
    Optional<Bed> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
