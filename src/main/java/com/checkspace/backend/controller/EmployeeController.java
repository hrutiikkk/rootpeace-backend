package com.checkspace.backend.controller;

import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.dto.response.LeadResponse;
import com.checkspace.backend.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/leads/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getMyLeads(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getMyLeads(employeeId), "Your assigned leads"));
    }

    @PostMapping("/leads/{propertyId}/reveal-phone")
    public ResponseEntity<ApiResponse<String>> revealPhone(
            @PathVariable Long propertyId,
            @RequestParam Long employeeId,
            HttpServletRequest request) {
        String phone = employeeService.revealSellerPhone(employeeId, propertyId, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.ok(phone, "Phone revealed — logged in audit trail"));
    }
}