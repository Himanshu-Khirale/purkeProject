package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.emr.MedicalRecordDto;
import com.hms.security.UserPrincipal;
import com.hms.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emr")
@RequiredArgsConstructor
@Tag(name = "EMR", description = "Electronic Medical Records APIs")
public class EmrController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/patients/{patientId}/records")
    @PreAuthorize("hasAuthority('emr:view')")
    @Operation(summary = "Get medical records for a patient")
    public ResponseEntity<ApiResponse<Page<MedicalRecordDto>>> getByPatient(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        Page<MedicalRecordDto> records = medicalRecordService.getRecordsByPatient(principal, patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved"));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasAuthority('emr:view')")
    @Operation(summary = "Get medical record by ID")
    public ResponseEntity<ApiResponse<MedicalRecordDto>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                medicalRecordService.getRecord(id, principal), "Record retrieved"));
    }
}
