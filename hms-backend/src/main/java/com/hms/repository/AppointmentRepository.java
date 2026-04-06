package com.hms.repository;

import com.hms.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Page<Appointment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Appointment> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Page<Appointment> findByTenantIdAndDoctorIdAndAppointmentDateAndDeletedAtIsNull(
            UUID tenantId, UUID doctorId, LocalDate date, Pageable pageable);

    Page<Appointment> findByTenantIdAndPatientIdAndDeletedAtIsNull(
            UUID tenantId, UUID patientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.tenantId = :tenantId AND a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date AND a.deletedAt IS NULL AND a.status != 'cancelled' " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findConflicting(@Param("tenantId") UUID tenantId, @Param("doctorId") UUID doctorId,
                                       @Param("date") LocalDate date, @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tenantId = :tenantId AND a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date AND a.deletedAt IS NULL AND a.status != 'cancelled'")
    int countByDoctorAndDate(@Param("tenantId") UUID tenantId, @Param("doctorId") UUID doctorId,
                             @Param("date") LocalDate date);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);

    long countByTenantIdAndAppointmentDateAndDeletedAtIsNull(UUID tenantId, LocalDate date);
}
