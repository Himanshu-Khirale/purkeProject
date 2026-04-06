package com.hms.repository;

import com.hms.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Page<Medicine> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<Medicine> findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String name, Pageable pageable);
    Optional<Medicine> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
