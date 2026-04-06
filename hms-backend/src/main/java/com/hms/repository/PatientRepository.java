package com.hms.repository;

import com.hms.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Page<Patient> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Patient> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Optional<Patient> findByMrnAndTenantIdAndDeletedAtIsNull(String mrn, UUID tenantId);

    @Query("SELECT p FROM Patient p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
           "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.mrn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR p.phone LIKE CONCAT('%', :search, '%'))")
    Page<Patient> searchPatients(@Param("tenantId") UUID tenantId, @Param("search") String search, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
