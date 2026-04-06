package com.hms.dto.appointment;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateAppointmentRequest {
    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String type;
    private String reason;
    private String notes;
}
