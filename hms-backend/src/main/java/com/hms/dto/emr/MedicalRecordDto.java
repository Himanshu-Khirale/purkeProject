package com.hms.dto.emr;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MedicalRecordDto {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private UUID appointmentId;
    private LocalDate recordDate;
    private String chiefComplaint;
    private String diagnosis;
    private String treatmentPlan;
    private String prescriptions;
    private String vitals;
    private LocalDate followUpDate;
    private String status;
}
