package com.hms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "medical_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord extends BaseEntity {

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "chief_complaint")
    private String chiefComplaint;

    private String diagnosis;

    @Column(name = "treatment_plan")
    private String treatmentPlan;

    @Column(length = 4000)
    private String prescriptions;

    @Column(length = 4000)
    private String vitals;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    private String status;

    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;
}
