package com.hms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Getter
@Setter
public class Medicine extends BaseEntity {
    private String name;
    private String category;
    private String manufacturer;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private BigDecimal price;
    private LocalDate expiryDate;
    private String unit; // e.g., Tablet, Syrup
}
