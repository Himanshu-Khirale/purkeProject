package com.hms.service;

import com.hms.dto.patient.*;
import com.hms.entity.Patient;
import com.hms.exception.*;
import com.hms.repository.PatientRepository;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private static final AtomicLong mrnCounter = new AtomicLong(System.currentTimeMillis() % 10000);

    @Transactional(readOnly = true)
    public Page<PatientDto> getAllPatients(UserPrincipal principal, String search, Pageable pageable) {
        Page<Patient> patients;
        if (search != null && !search.isBlank()) {
            patients = patientRepository.searchPatients(principal.getTenantId(), search, pageable);
        } else {
            patients = patientRepository.findByTenantIdAndDeletedAtIsNull(principal.getTenantId(), pageable);
        }
        return patients.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public PatientDto getPatient(UUID id, UserPrincipal principal) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        return toDto(patient);
    }

    @Transactional
    public PatientDto createPatient(CreatePatientRequest request, UserPrincipal principal) {
        String mrn = generateMrn();

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .phone(request.getPhone())
                .email(request.getEmail())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .insuranceProvider(request.getInsuranceProvider())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .notes(request.getNotes())
                .isActive(true)
                .build();
        patient.setTenantId(principal.getTenantId());
        patient.setMrn(mrn);

        Patient saved = patientRepository.save(patient);
        return toDto(saved);
    }

    @Transactional
    public PatientDto updatePatient(UUID id, CreatePatientRequest request, UserPrincipal principal) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setEmergencyContactName(request.getEmergencyContactName());
        patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        if (request.getCountry() != null) patient.setCountry(request.getCountry());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        patient.setNotes(request.getNotes());

        Patient saved = patientRepository.save(patient);
        return toDto(saved);
    }

    @Transactional
    public void deletePatient(UUID id, UserPrincipal principal) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
        patient.setDeletedAt(OffsetDateTime.now());
        patientRepository.save(patient);
    }

    private String generateMrn() {
        return String.format("MRN-%d-%04d", java.time.LocalDate.now().getYear(), mrnCounter.incrementAndGet());
    }

    private PatientDto toDto(Patient p) {
        return PatientDto.builder()
                .id(p.getId())
                .mrn(p.getMrn())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .bloodGroup(p.getBloodGroup())
                .phone(p.getPhone())
                .email(p.getEmail())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .zipCode(p.getZipCode())
                .country(p.getCountry())
                .insuranceProvider(p.getInsuranceProvider())
                .insurancePolicyNumber(p.getInsurancePolicyNumber())
                .notes(p.getNotes())
                .isActive(p.getIsActive())
                .build();
    }
}
