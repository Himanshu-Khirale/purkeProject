package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.inpatient.BedDto;
import com.hms.dto.inpatient.WardDto;
import com.hms.security.UserPrincipal;
import com.hms.service.InpatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inpatient")
@RequiredArgsConstructor
public class InpatientController {

    private final InpatientService inpatientService;

    @GetMapping("/wards")
    public ResponseEntity<ApiResponse<List<WardDto>>> getWards(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<WardDto> wards = inpatientService.getWards(principal.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(wards, "Success"));
    }

    @GetMapping("/beds")
    public ResponseEntity<ApiResponse<List<BedDto>>> getBeds(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID wardId) {
        List<BedDto> beds = inpatientService.getBeds(principal.getTenantId(), wardId);
        return ResponseEntity.ok(ApiResponse.success(beds, "Success"));
    }
}
