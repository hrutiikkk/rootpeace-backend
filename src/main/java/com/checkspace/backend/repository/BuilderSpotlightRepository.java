package com.checkspace.backend.repository;

import com.checkspace.backend.model.BuilderSpotlight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BuilderSpotlightRepository extends JpaRepository<BuilderSpotlight, Long> {
    List<BuilderSpotlight> findByActiveTrueOrderByCreatedAtDesc();
}