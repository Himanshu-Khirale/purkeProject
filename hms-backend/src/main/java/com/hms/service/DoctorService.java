package com.hms.service;

import com.hms.dto.doctor.DoctorDto;
import com.hms.entity.Doctor;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.DoctorRepository;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Page<DoctorDto> getAllDoctors(UserPrincipal principal, UUID departmentId, Pageable pageable) {
        Page<Doctor> doctors;
        if (departmentId != null) {
            doctors = doctorRepository.findByTenantIdAndDepartmentIdAndDeletedAtIsNull(
                    principal.getTenantId(), departmentId, pageable);
        } else {
            doctors = doctorRepository.findByTenantIdAndDeletedAtIsNull(principal.getTenantId(), pageable);
        }
        return doctors.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctor(UUID id, UserPrincipal principal) {
        Doctor doctor = doctorRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return toDto(doctor);
    }

    private DoctorDto toDto(Doctor d) {
        return DoctorDto.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .firstName(d.getUser() != null ? d.getUser().getFirstName() : null)
                .lastName(d.getUser() != null ? d.getUser().getLastName() : null)
                .email(d.getUser() != null ? d.getUser().getEmail() : null)
                .phone(d.getUser() != null ? d.getUser().getPhone() : null)
                .specialization(d.getSpecialization())
                .qualification(d.getQualification())
                .licenseNumber(d.getLicenseNumber())
                .experienceYears(d.getExperienceYears())
                .consultationFee(d.getConsultationFee())
                .bio(d.getBio())
                .isAvailable(d.getIsAvailable())
                .departmentId(d.getDepartmentId())
                .departmentName(d.getDepartment() != null ? d.getDepartment().getName() : null)
                .build();
    }
}
