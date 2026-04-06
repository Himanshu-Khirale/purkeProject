package com.hms.dto.pharmacy;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MedicineDto {
    private String id;
    private String name;
    private String category;
    private String manufacturer;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private BigDecimal price;
    private LocalDate expiryDate;
    private String unit;
}
