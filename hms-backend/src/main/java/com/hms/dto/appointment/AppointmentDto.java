package com.hms.dto.appointment;

import lombok.*;
import java.time.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AppointmentDto {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String type;
    private String reason;
    private String notes;
    private Integer tokenNumber;
}
