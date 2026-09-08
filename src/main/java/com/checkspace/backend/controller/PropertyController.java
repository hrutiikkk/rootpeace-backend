package com.checkspace.backend.controller;

import com.checkspace.backend.dto.request.CreatePropertyRequest;
import com.checkspace.backend.dto.response.ApiResponse;
import com.checkspace.backend.dto.response.PropertyResponse;
import com.checkspace.backend.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PutMapping("/{propertyId}/withdraw")
    public ResponseEntity<ApiResponse<PropertyResponse>> withdraw(
            @PathVariable Long propertyId,
            @RequestParam Long sellerId) {
        try {
            PropertyResponse response = propertyService.withdrawProperty(propertyId, sellerId);
            return ResponseEntity.ok(ApiResponse.ok(response, "Property withdrawn successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // SELLER — create new listing
    @PostMapping
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody CreatePropertyRequest request) {
        PropertyResponse response = propertyService.createProperty(request);
        return ResponseEntity.ok(
                ApiResponse.ok(response, "Property submitted for verification")
        );
    }

    // BUYER — public listings, paginated
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getPublicListings(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                propertyService.getPublicListings(q, PageRequest.of(page, size)),
                "Listings fetched"));
    }

    // ANYONE — single property detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getById(@PathVariable Long id) {
        PropertyResponse response = propertyService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Property found"));
    }

    // SELLER — my own listings
    @GetMapping("/my/{sellerId}")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyListings(
            @PathVariable Long sellerId) {
        List<PropertyResponse> listings = propertyService.getMyListings(sellerId);
        return ResponseEntity.ok(ApiResponse.ok(listings, "Your listings"));
    }

    // ADMIN — approve after verification
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PropertyResponse>> approve(@PathVariable Long id) {
        PropertyResponse response = propertyService.approveListing(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Property approved and live"));
    }

    // ADMIN — reject if verification fails
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<PropertyResponse>> reject(@PathVariable Long id) {
        PropertyResponse response = propertyService.rejectListing(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Property rejected"));
    }


    @PutMapping("/{propertyId}/relist")
    public ResponseEntity<ApiResponse<PropertyResponse>> relist(
            @PathVariable Long propertyId,
            @RequestParam Long sellerId) {

        // The Controller just hands the request to the Service!
        PropertyResponse response = propertyService.relistProperty(propertyId, sellerId);

        return ResponseEntity.ok(ApiResponse.ok(
                response, "Property relisted successfully"));
    }
}