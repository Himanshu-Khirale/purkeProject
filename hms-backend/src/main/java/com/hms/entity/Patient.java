package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient extends BaseEntity {

    @Column(name = "user_id")
    private java.util.UUID userId;

    @Column(nullable = false)
    private String mrn;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private java.time.LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender;

    @Column(name = "blood_group")
    private String bloodGroup;

    private String phone;
    private String email;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    private String address;
    private String city;
    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    private String country;

    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Column(length = 2000)
    private String allergies;

    @Column(name = "chronic_conditions", length = 2000)
    private String chronicConditions;

    @Column(length = 2000)
    private String tags;

    private String notes;

    @Column(name = "is_active")
    private Boolean isActive;
}
