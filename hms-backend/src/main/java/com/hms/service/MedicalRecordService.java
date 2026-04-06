package com.hms.service;

import com.hms.dto.emr.MedicalRecordDto;
import com.hms.entity.MedicalRecord;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.MedicalRecordRepository;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> getRecordsByPatient(UserPrincipal principal, UUID patientId, Pageable pageable) {
        Page<MedicalRecord> records = medicalRecordRepository
                .findByTenantIdAndPatientIdAndDeletedAtIsNull(principal.getTenantId(), patientId, pageable);
        return records.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public MedicalRecordDto getRecord(UUID id, UserPrincipal principal) {
        MedicalRecord record = medicalRecordRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", "id", id));
        return toDto(record);
    }

    private MedicalRecordDto toDto(MedicalRecord r) {
        return MedicalRecordDto.builder()
                .id(r.getId())
                .patientId(r.getPatientId())
                .patientName(r.getPatient() != null
                        ? r.getPatient().getFirstName() + " " + r.getPatient().getLastName() : null)
                .doctorId(r.getDoctorId())
                .doctorName(r.getDoctor() != null && r.getDoctor().getUser() != null
                        ? "Dr. " + r.getDoctor().getUser().getFirstName() + " " + r.getDoctor().getUser().getLastName()
                        : null)
                .appointmentId(r.getAppointmentId())
                .recordDate(r.getRecordDate())
                .chiefComplaint(r.getChiefComplaint())
                .diagnosis(r.getDiagnosis())
                .treatmentPlan(r.getTreatmentPlan())
                .prescriptions(r.getPrescriptions())
                .vitals(r.getVitals())
                .followUpDate(r.getFollowUpDate())
                .status(r.getStatus())
                .build();
    }
}
