package com.hms.repository;

import com.hms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<Department> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
