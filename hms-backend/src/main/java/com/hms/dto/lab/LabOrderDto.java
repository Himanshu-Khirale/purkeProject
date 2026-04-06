package com.hms.dto.lab;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class LabOrderDto {
    private String id;
    private String orderNumber;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String testName;
    private String category;
    private String priority;
    private String status;
    private String result;
    private LocalDateTime resultDate;
}
