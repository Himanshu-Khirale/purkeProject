package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.billing.InvoiceDto;
import com.hms.security.UserPrincipal;
import com.hms.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Billing and invoice APIs")
public class BillingController {

    private final InvoiceService invoiceService;

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('billing:view')")
    @Operation(summary = "Get invoices with pagination")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> getInvoices(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("invoiceDate").descending());
        Page<InvoiceDto> invoices = invoiceService.getInvoices(principal, patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved"));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('billing:view')")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceDto>> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoice(id, principal), "Invoice retrieved"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('billing:view')")
    @Operation(summary = "Get billing stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getBillingStats(principal), "Billing stats retrieved"));
    }
}
