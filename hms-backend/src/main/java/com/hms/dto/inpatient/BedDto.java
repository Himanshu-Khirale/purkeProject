package com.hms.dto.inpatient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BedDto {
    private String id;
    private String bedNumber;
    private String wardId;
    private String wardName;
    private String status;
    private String patientId;
    private String patientName;
}
