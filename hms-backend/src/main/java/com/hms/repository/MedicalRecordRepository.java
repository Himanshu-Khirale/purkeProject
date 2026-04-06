package com.hms.repository;

import com.hms.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    Page<MedicalRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<MedicalRecord> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<MedicalRecord> findByTenantIdAndPatientIdAndDeletedAtIsNull(UUID tenantId, UUID patientId, Pageable pageable);
    Page<MedicalRecord> findByTenantIdAndDoctorIdAndDeletedAtIsNull(UUID tenantId, UUID doctorId, Pageable pageable);
}
