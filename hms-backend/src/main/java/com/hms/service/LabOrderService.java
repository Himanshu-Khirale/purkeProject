package com.hms.service;

import com.hms.dto.lab.LabOrderDto;
import com.hms.entity.LabOrder;
import com.hms.repository.LabOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;

    public Page<LabOrderDto> getOrders(UUID tenantId, Pageable pageable) {
        return labOrderRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::mapToDto);
    }

    private LabOrderDto mapToDto(LabOrder order) {
        LabOrderDto dto = new LabOrderDto();
        dto.setId(order.getId().toString());
        dto.setOrderNumber(order.getOrderNumber());
        if (order.getPatient() != null) {
            dto.setPatientId(order.getPatient().getId().toString());
            dto.setPatientName(order.getPatient().getFirstName() + " " + order.getPatient().getLastName());
        }
        if (order.getDoctor() != null && order.getDoctor().getUser() != null) {
            dto.setDoctorId(order.getDoctor().getId().toString());
            dto.setDoctorName(order.getDoctor().getUser().getFirstName() + " " + order.getDoctor().getUser().getLastName());
        }
        dto.setTestName(order.getTestName());
        dto.setCategory(order.getCategory());
        dto.setPriority(order.getPriority());
        dto.setStatus(order.getStatus());
        dto.setResult(order.getResult());
        dto.setResultDate(order.getResultDate());
        return dto;
    }
}
