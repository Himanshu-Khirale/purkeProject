package com.hms.dto.patient;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreatePatientRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    private String bloodGroup;

    @Pattern(regexp = "^\\+?[0-9\\-]{10,15}$", message = "Invalid phone format")
    private String phone;

    @Email(message = "Invalid email format")
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
}
