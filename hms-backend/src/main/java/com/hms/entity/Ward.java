package com.hms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wards")
@Getter
@Setter
public class Ward extends BaseEntity {
    private String name;
    private String type; // GENERAL, ICU, PRIVATE, SEMI_PRIVATE
    private Integer totalBeds;
}
