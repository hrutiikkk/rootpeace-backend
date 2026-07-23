package com.checkspace.backend.repository;

import com.checkspace.backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<AuditLog> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
}