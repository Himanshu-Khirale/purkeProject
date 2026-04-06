package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.security.UserPrincipal;
import com.hms.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard analytics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('reports:view_dashboard')")
    @Operation(summary = "Get dashboard KPIs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> stats = dashboardService.getDashboardStats(principal);
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard stats retrieved"));
    }
}
