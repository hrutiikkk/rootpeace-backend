package com.checkspace.backend.controller;

import com.checkspace.backend.dto.request.AssignLeadRequest;
import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.dto.response.DashboardStatsResponse;
import com.checkspace.backend.dto.response.PropertyResponse;
import com.checkspace.backend.model.User;
import com.checkspace.backend.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/assign-lead")
    public ResponseEntity<ApiResponse<PropertyResponse>> assignLead(
            @Valid @RequestBody AssignLeadRequest request,
            @RequestParam Long adminId,
            HttpServletRequest httpRequest) {
        PropertyResponse response = adminService.assignLead(
                request.getPropertyId(), request.getEmployeeId(), adminId, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(response, "Lead assigned"));
    }
    @GetMapping("/properties/all")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> allProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getAllProperties(PageRequest.of(page, size)), "All properties"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> allUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getAllUsers(PageRequest.of(page, size)), "All users"));
    }

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<User>>> getEmployees() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllEmployees(), "Employee list"));
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> pendingVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getPendingVerifications(PageRequest.of(page, size)), "Pending verifications"));
    }

    @PutMapping("/properties/{id}/mark-sold")
    public ResponseEntity<ApiResponse<PropertyResponse>> markSold(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.markAsSold(id), "Property marked as sold"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats(), "Dashboard stats"));
    }
}