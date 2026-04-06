package com.hms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "beds")
@Getter
@Setter
public class Bed extends BaseEntity {
    private String bedNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;
    
    private String status; // AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
