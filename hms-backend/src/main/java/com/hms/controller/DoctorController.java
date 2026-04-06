package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.doctor.DoctorDto;
import com.hms.security.UserPrincipal;
import com.hms.service.DoctorService;
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
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management APIs")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAuthority('doctors:view')")
    @Operation(summary = "Get all doctors with pagination")
    public ResponseEntity<ApiResponse<Page<DoctorDto>>> getAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DoctorDto> doctors = doctorService.getAllDoctors(principal, departmentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(doctors, "Doctors retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('doctors:view')")
    @Operation(summary = "Get doctor by ID")
    public ResponseEntity<ApiResponse<DoctorDto>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctor(id, principal), "Doctor retrieved"));
    }
}
