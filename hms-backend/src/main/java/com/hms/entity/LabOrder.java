package com.hms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_orders")
@Getter
@Setter
public class LabOrder extends BaseEntity {
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    private String testName;
    private String category;
    private String priority; // NORMAL, URGENT, STAT
    private String status; // PENDING, PROCESSING, COMPLETED, CRITICAL
    private String result;
    private LocalDateTime resultDate;
}
