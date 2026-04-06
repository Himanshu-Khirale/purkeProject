package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.lab.LabOrderDto;
import com.hms.security.UserPrincipal;
import com.hms.service.LabOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lab")
@RequiredArgsConstructor
public class LabOrderController {

    private final LabOrderService labOrderService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<LabOrderDto>>> getOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<LabOrderDto> orders = labOrderService.getOrders(principal.getTenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders, "Success"));
    }
}
