package com.checkspace.backend.service;

import com.checkspace.backend.dto.response.DashboardStatsResponse;
import com.checkspace.backend.dto.response.PropertyResponse;
import com.checkspace.backend.model.AuditLog;
import com.checkspace.backend.model.Property;
import com.checkspace.backend.model.User;
import com.checkspace.backend.repository.AuditLogRepository;
import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import com.checkspace.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final PropertyRepository propertyRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private static final int MAX_LEADS_PER_EMPLOYEE = 50;

    public PropertyResponse assignLead(Long propertyId, Long employeeId, Long adminId, String ipAddress) {
        long currentLoad = propertyRepository.countByAssignedTo(employeeId);
        if (currentLoad >= MAX_LEADS_PER_EMPLOYEE) {
            throw new RuntimeException(
                    "Employee already has " + MAX_LEADS_PER_EMPLOYEE + " leads. Assign to someone else.");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setAssignedTo(employeeId);
        Property saved = propertyRepository.save(property);

        auditLogRepository.save(AuditLog.builder()
                .employeeId(adminId)
                .propertyId(propertyId)
                .action("ASSIGNED_LEAD_TO_" + employeeId)
                .ipAddress(ipAddress)
                .build());

        return PropertyResponse.from(saved);
    }

    public Page<PropertyResponse> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(PropertyResponse::from);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<PropertyResponse> getPendingVerifications(Pageable pageable) {
        return propertyRepository.findByStatus(Property.PropertyStatus.PENDING, pageable)
                .map(PropertyResponse::from);
    }

    public List<User> getAllEmployees() {
        return userRepository.findByRole(User.UserRole.EMPLOYEE);
    }

    public PropertyResponse markAsSold(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        property.setStatus(Property.PropertyStatus.SOLD);
        property.setSoldAt(LocalDateTime.now());
        return PropertyResponse.from(propertyRepository.save(property));
    }

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime yearStart  = LocalDate.now().withDayOfYear(1).atStartOfDay();

        return DashboardStatsResponse.builder()
                .listingsToday(propertyRepository.countByCreatedAtBetween(todayStart, todayEnd))
                .listingsThisMonth(propertyRepository.countByCreatedAtBetween(monthStart, todayEnd))
                .dealsClosedToday(propertyRepository.countByStatusAndSoldAtBetween(
                        Property.PropertyStatus.SOLD, todayStart, todayEnd))
                .dealsClosedThisMonth(propertyRepository.countByStatusAndSoldAtBetween(
                        Property.PropertyStatus.SOLD, monthStart, todayEnd))
                .revenueToday(paymentRepository.sumSuccessfulPaymentsBetween(todayStart, todayEnd))
                .revenueThisMonth(paymentRepository.sumSuccessfulPaymentsBetween(monthStart, todayEnd))
                .revenueThisYear(paymentRepository.sumSuccessfulPaymentsBetween(yearStart, todayEnd))
                .pendingVerifications(propertyRepository.countByStatus(Property.PropertyStatus.PENDING))
                .activeListings(propertyRepository.countByStatus(Property.PropertyStatus.ACTIVE))
                .build();
    }
}