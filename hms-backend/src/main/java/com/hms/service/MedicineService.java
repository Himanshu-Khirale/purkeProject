package com.hms.service;

import com.hms.dto.pharmacy.MedicineDto;
import com.hms.entity.Medicine;
import com.hms.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public Page<MedicineDto> getInventory(UUID tenantId, String search, Pageable pageable) {
        Page<Medicine> medicines;
        if (search != null && !search.isEmpty()) {
            medicines = medicineRepository.findByTenantIdAndNameContainingIgnoreCaseAndDeletedAtIsNull(tenantId, search, pageable);
        } else {
            medicines = medicineRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return medicines.map(this::mapToDto);
    }

    private MedicineDto mapToDto(Medicine medicine) {
        MedicineDto dto = new MedicineDto();
        dto.setId(medicine.getId().toString());
        dto.setName(medicine.getName());
        dto.setCategory(medicine.getCategory());
        dto.setManufacturer(medicine.getManufacturer());
        dto.setStockQuantity(medicine.getStockQuantity());
        dto.setReorderLevel(medicine.getReorderLevel());
        dto.setPrice(medicine.getPrice());
        dto.setExpiryDate(medicine.getExpiryDate());
        dto.setUnit(medicine.getUnit());
        return dto;
    }
}
