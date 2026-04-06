package com.hms.repository;

import com.hms.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<Invoice> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
    Page<Invoice> findByTenantIdAndPatientIdAndDeletedAtIsNull(UUID tenantId, UUID patientId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.deletedAt IS NULL AND i.status != 'cancelled'")
    BigDecimal getTotalRevenue(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.deletedAt IS NULL")
    BigDecimal getTotalCollected(@Param("tenantId") UUID tenantId);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, String status);
}
