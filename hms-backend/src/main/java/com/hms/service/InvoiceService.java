package com.hms.service;

import com.hms.dto.billing.InvoiceDto;
import com.hms.entity.Invoice;
import com.hms.exception.ResourceNotFoundException;
import com.hms.repository.InvoiceRepository;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoices(UserPrincipal principal, UUID patientId, Pageable pageable) {
        Page<Invoice> invoices;
        if (patientId != null) {
            invoices = invoiceRepository.findByTenantIdAndPatientIdAndDeletedAtIsNull(
                    principal.getTenantId(), patientId, pageable);
        } else {
            invoices = invoiceRepository.findByTenantIdAndDeletedAtIsNull(principal.getTenantId(), pageable);
        }
        return invoices.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoice(UUID id, UserPrincipal principal) {
        Invoice inv = invoiceRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, principal.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return toDto(inv);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBillingStats(UserPrincipal principal) {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenue(principal.getTenantId());
        BigDecimal totalCollected = invoiceRepository.getTotalCollected(principal.getTenantId());
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCollected", totalCollected);
        stats.put("pendingAmount", totalRevenue.subtract(totalCollected));
        stats.put("overdueCount", invoiceRepository.countByTenantIdAndStatusAndDeletedAtIsNull(
                principal.getTenantId(), "overdue"));
        stats.put("pendingCount", invoiceRepository.countByTenantIdAndStatusAndDeletedAtIsNull(
                principal.getTenantId(), "sent"));
        return stats;
    }

    private InvoiceDto toDto(Invoice i) {
        BigDecimal paid = i.getPaidAmount() != null ? i.getPaidAmount() : BigDecimal.ZERO;
        return InvoiceDto.builder()
                .id(i.getId())
                .patientId(i.getPatientId())
                .patientName(i.getPatient() != null
                        ? i.getPatient().getFirstName() + " " + i.getPatient().getLastName() : null)
                .invoiceNumber(i.getInvoiceNumber())
                .invoiceDate(i.getInvoiceDate())
                .dueDate(i.getDueDate())
                .subtotal(i.getSubtotal())
                .taxAmount(i.getTaxAmount())
                .discountAmount(i.getDiscountAmount())
                .totalAmount(i.getTotalAmount())
                .paidAmount(paid)
                .balance(i.getTotalAmount().subtract(paid))
                .status(i.getStatus())
                .notes(i.getNotes())
                .build();
    }
}
