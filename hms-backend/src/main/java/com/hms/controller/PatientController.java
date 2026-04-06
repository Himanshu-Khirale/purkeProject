package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.patient.*;
import com.hms.security.UserPrincipal;
import com.hms.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient management APIs")
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAuthority('patients:view')")
    @Operation(summary = "Get all patients with pagination and search")
    public ResponseEntity<ApiResponse<Page<PatientDto>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientDto> patients = patientService.getAllPatients(principal, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(patients, "Patients retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('patients:view')")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<ApiResponse<PatientDto>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        PatientDto patient = patientService.getPatient(id, principal);
        return ResponseEntity.ok(ApiResponse.success(patient, "Patient retrieved"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('patients:create')")
    @Operation(summary = "Create a new patient")
    public ResponseEntity<ApiResponse<PatientDto>> create(
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PatientDto patient = patientService.createPatient(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(patient, "Patient created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('patients:update')")
    @Operation(summary = "Update an existing patient")
    public ResponseEntity<ApiResponse<PatientDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePatientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PatientDto patient = patientService.updatePatient(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success(patient, "Patient updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('patients:delete')")
    @Operation(summary = "Soft delete a patient")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        patientService.deletePatient(id, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient deleted"));
    }
}
