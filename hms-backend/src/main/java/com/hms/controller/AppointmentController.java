package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.appointment.*;
import com.hms.security.UserPrincipal;
import com.hms.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment scheduling APIs")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('appointments:view')")
    @Operation(summary = "Get appointments with filters")
    public ResponseEntity<ApiResponse<Page<AppointmentDto>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AppointmentDto> appointments = appointmentService.getAppointments(principal, doctorId, patientId, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(appointments, "Appointments retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('appointments:view')")
    @Operation(summary = "Get appointment by ID")
    public ResponseEntity<ApiResponse<AppointmentDto>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(appointmentService.getAppointment(id, principal), "Appointment retrieved"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('appointments:create')")
    @Operation(summary = "Create a new appointment")
    public ResponseEntity<ApiResponse<AppointmentDto>> create(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AppointmentDto created = appointmentService.createAppointment(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Appointment created"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('appointments:update')")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<ApiResponse<AppointmentDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        AppointmentDto updated = appointmentService.updateStatus(id, body.get("status"), principal);
        return ResponseEntity.ok(ApiResponse.success(updated, "Appointment status updated"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('appointments:cancel')")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancel(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        AppointmentDto cancelled = appointmentService.cancelAppointment(id, body.get("reason"), principal);
        return ResponseEntity.ok(ApiResponse.success(cancelled, "Appointment cancelled"));
    }
}
