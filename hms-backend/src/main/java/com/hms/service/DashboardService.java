package com.hms.service;

import com.hms.repository.*;
import com.hms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(UserPrincipal principal) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPatients", patientRepository.countByTenantIdAndDeletedAtIsNull(principal.getTenantId()));
        stats.put("totalDoctors", doctorRepository.countByTenantIdAndDeletedAtIsNull(principal.getTenantId()));
        stats.put("totalAppointments", appointmentRepository.countByTenantIdAndDeletedAtIsNull(principal.getTenantId()));
        stats.put("todayAppointments", appointmentRepository.countByTenantIdAndAppointmentDateAndDeletedAtIsNull(
                principal.getTenantId(), LocalDate.now()));

        BigDecimal totalRevenue = invoiceRepository.getTotalRevenue(principal.getTenantId());
        BigDecimal totalCollected = invoiceRepository.getTotalCollected(principal.getTenantId());
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCollected", totalCollected);
        stats.put("pendingAmount", totalRevenue.subtract(totalCollected));

        stats.put("pendingInvoices", invoiceRepository.countByTenantIdAndStatusAndDeletedAtIsNull(
                principal.getTenantId(), "sent"));
        stats.put("overdueInvoices", invoiceRepository.countByTenantIdAndStatusAndDeletedAtIsNull(
                principal.getTenantId(), "overdue"));

        return stats;
    }
}
