package com.hms.dto.doctor;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DoctorDto {
    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private String licenseNumber;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private Boolean isAvailable;
    private String departmentName;
    private UUID departmentId;
}
