package com.hms.repository;

import com.hms.entity.LabOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {
    Page<LabOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<LabOrder> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
