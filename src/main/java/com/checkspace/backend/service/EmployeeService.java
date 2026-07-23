package com.checkspace.backend.service;

import com.checkspace.backend.dto.response.LeadResponse;
import com.checkspace.backend.model.AuditLog;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.model.User;
import com.checkspace.backend.repository.AuditLogRepository;
import com.checkspace.backend.repository.PropertyRepository;
import com.checkspace.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public List<LeadResponse> getMyLeads(Long employeeId) {
        List<Property> leads = propertyRepository.findByAssignedToOrderByCreatedAtDesc(employeeId);
        return leads.stream()
                .map(p -> {
                    String phone = userRepository.findById(p.getSellerId())
                            .map(User::getPhone).orElse("0000000000");
                    return LeadResponse.from(p, phone);
                })
                .collect(Collectors.toList());
    }

    // Real phone only revealed here — every single reveal is logged, no exceptions
    public String revealSellerPhone(Long employeeId, Long propertyId, String ipAddress) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!employeeId.equals(property.getAssignedTo())) {
            throw new RuntimeException("This lead is not assigned to you");
        }

        User seller = userRepository.findById(property.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        auditLogRepository.save(AuditLog.builder()
                .employeeId(employeeId)
                .propertyId(propertyId)
                .action("VIEWED_PHONE")
                .ipAddress(ipAddress)
                .build());

        return seller.getPhone();
    }
}