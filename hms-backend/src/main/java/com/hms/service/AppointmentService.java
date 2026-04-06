package com.hms.service;

import com.hms.dto.appointment.*;
import com.hms.entity.*;
import com.hms.exception.*;
import com.hms.repository.*;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Page<AppointmentDto> getAppointments(UserPrincipal principal, UUID doctorId, UUID patientId, LocalDate date, Pageable pageable) {
        Page<Appointment> appointments;
        if (doctorId != null && date != null) {
            appointments = appointmentRepository.findByTenantIdAndDoctorIdAndAppointmentDateAndDeletedAtIsNull(
                    principal.getTenantId(), doctorId, date, pageable);
        } else if (patientId != null) {
            appointments = appointmentRepository.findByTenantIdAndPatientIdAndDeletedAtIsNull(
                    principal.getTenantId(), patientId, pageable);
        } else {
            appointments = appointmentRepository.findByTenantIdAndDeletedAtIsNull(principal.getTenantId(), pageable);
        }
        return appointments.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AppointmentDto getAppointment(UUID id, UserPrincipal principal) {
        Appointment apt = appointmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return toDto(apt);
    }

    @Transactional
    public AppointmentDto createAppointment(CreateAppointmentRequest request, UserPrincipal principal) {
        patientRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getPatientId(), principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        doctorRepository.findByIdAndTenantIdAndDeletedAtIsNull(request.getDoctorId(), principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId()));

        List<Appointment> conflicts = appointmentRepository.findConflicting(
                principal.getTenantId(), request.getDoctorId(), request.getAppointmentDate(),
                request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Doctor already has an appointment in this time slot");
        }

        int tokenNumber = appointmentRepository.countByDoctorAndDate(
                principal.getTenantId(), request.getDoctorId(), request.getAppointmentDate()) + 1;

        Appointment appointment = Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status("scheduled")
                .type(request.getType() != null ? request.getType() : "consultation")
                .reason(request.getReason())
                .notes(request.getNotes())
                .tokenNumber(tokenNumber)
                .build();
        appointment.setTenantId(principal.getTenantId());

        Appointment saved = appointmentRepository.save(appointment);
        return toDto(saved);
    }

    @Transactional
    public AppointmentDto updateStatus(UUID id, String status, UserPrincipal principal) {
        Appointment apt = appointmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        apt.setStatus(status);
        Appointment saved = appointmentRepository.save(apt);
        return toDto(saved);
    }

    @Transactional
    public AppointmentDto cancelAppointment(UUID id, String reason, UserPrincipal principal) {
        Appointment apt = appointmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if ("completed".equals(apt.getStatus())) {
            throw new BadRequestException("Cannot cancel a completed appointment");
        }

        apt.setStatus("cancelled");
        apt.setCancellationReason(reason);
        apt.setCancelledBy(principal.getUserId());
        Appointment saved = appointmentRepository.save(apt);
        return toDto(saved);
    }

    private AppointmentDto toDto(Appointment a) {
        String patientName = null;
        String doctorName = null;
        if (a.getPatient() != null) {
            patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
        }
        if (a.getDoctor() != null && a.getDoctor().getUser() != null) {
            doctorName = "Dr. " + a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName();
        }

        return AppointmentDto.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .patientName(patientName)
                .doctorId(a.getDoctorId())
                .doctorName(doctorName)
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .type(a.getType())
                .reason(a.getReason())
                .notes(a.getNotes())
                .tokenNumber(a.getTokenNumber())
                .build();
    }
}
