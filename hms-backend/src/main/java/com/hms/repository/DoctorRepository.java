package com.hms.repository;

import com.hms.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Page<Doctor> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<Doctor> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    Optional<Doctor> findByUserIdAndTenantIdAndDeletedAtIsNull(UUID userId, UUID tenantId);
    Page<Doctor> findByTenantIdAndDepartmentIdAndDeletedAtIsNull(UUID tenantId, UUID departmentId, Pageable pageable);
    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
