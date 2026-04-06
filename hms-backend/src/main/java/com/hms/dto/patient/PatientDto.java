package com.hms.dto.patient;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientDto {
    private UUID id;
    private String mrn;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String email;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private String notes;
    private Boolean isActive;
}
