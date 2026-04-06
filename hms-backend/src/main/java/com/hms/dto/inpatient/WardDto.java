package com.hms.dto.inpatient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WardDto {
    private String id;
    private String name;
    private String type;
    private Integer totalBeds;
    private Long occupiedBeds;
}
