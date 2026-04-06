package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.pharmacy.MedicineDto;
import com.hms.security.UserPrincipal;
import com.hms.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pharmacy")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getInventory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<MedicineDto> inventory = medicineService.getInventory(principal.getTenantId(), search, pageable);
        return ResponseEntity.ok(ApiResponse.success(inventory, "Success"));
    }
}
