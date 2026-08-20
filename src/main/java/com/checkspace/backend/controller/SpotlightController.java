package com.checkspace.backend.controller;

import com.checkspace.backend.model.BuilderSpotlight;
import com.checkspace.backend.repository.BuilderSpotlightRepository;
// Note: Ensure this import matches where your ApiResponse class is located
import com.checkspace.backend.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SpotlightController {

    // This automatically injects the repository you created in the last step
    private final BuilderSpotlightRepository spotlightRepository;

    @GetMapping("/api/spotlights")
    public ResponseEntity<ApiResponse<List<BuilderSpotlight>>> getSpotlights() {
        return ResponseEntity.ok(ApiResponse.ok(
                spotlightRepository.findByActiveTrueOrderByCreatedAtDesc(),
                "Spotlights fetched"));
    }
}