package com.hms.service;

import com.hms.dto.inpatient.BedDto;
import com.hms.dto.inpatient.WardDto;
import com.hms.entity.Bed;
import com.hms.entity.Ward;
import com.hms.repository.BedRepository;
import com.hms.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InpatientService {

    private final WardRepository wardRepository;
    private final BedRepository bedRepository;

    public List<WardDto> getWards(UUID tenantId) {
        List<Ward> wards = wardRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<Bed> allBeds = bedRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        
        return wards.stream().map(w -> {
            WardDto dto = new WardDto();
            dto.setId(w.getId().toString());
            dto.setName(w.getName());
            dto.setType(w.getType());
            dto.setTotalBeds(w.getTotalBeds());
            long occupied = allBeds.stream()
                    .filter(b -> b.getWard().getId().equals(w.getId()) && "OCCUPIED".equals(b.getStatus()))
                    .count();
            dto.setOccupiedBeds(occupied);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<BedDto> getBeds(UUID tenantId, UUID wardId) {
        List<Bed> beds;
        if (wardId != null) {
            beds = bedRepository.findByWardIdAndTenantIdAndDeletedAtIsNull(wardId, tenantId);
        } else {
            beds = bedRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        }
        return beds.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private BedDto mapToDto(Bed bed) {
        BedDto dto = new BedDto();
        dto.setId(bed.getId().toString());
        dto.setBedNumber(bed.getBedNumber());
        if (bed.getWard() != null) {
            dto.setWardId(bed.getWard().getId().toString());
            dto.setWardName(bed.getWard().getName());
        }
        dto.setStatus(bed.getStatus());
        if (bed.getPatient() != null) {
            dto.setPatientId(bed.getPatient().getId().toString());
            dto.setPatientName(bed.getPatient().getFirstName() + " " + bed.getPatient().getLastName());
        }
        return dto;
    }
}
