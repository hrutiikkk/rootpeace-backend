package com.checkspace.backend.repository;

import com.checkspace.backend.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    long countByAssignedTo(Long employeeId);
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatusAndSoldAtBetween(Property.PropertyStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatus(Property.PropertyStatus status);

    long countByVisibleTrueAndStatus(Property.PropertyStatus status);
    long countByVisibleTrueAndStatusAndCity(Property.PropertyStatus status, String city);

    // Buyers see only ACTIVE visible listings
    Page<Property> findByVisibleTrueAndStatus(
            Property.PropertyStatus status, Pageable pageable);

    // Filter by city
    Page<Property> findByVisibleTrueAndStatusAndCity(
            Property.PropertyStatus status, String city, Pageable pageable);

    // Seller sees own listings
    List<Property> findBySellerId(Long sellerId);

    // Employee sees assigned leads (max 50)
    List<Property> findByAssignedToOrderByCreatedAtDesc(Long employeeId);

    // Admin sees pending verifications
    Page<Property> findByStatus(Property.PropertyStatus status, Pageable pageable);

    // Race condition protection — atomic check
    @Query("SELECT p FROM Property p WHERE p.id = :id AND p.status = 'ACTIVE'")
    java.util.Optional<Property> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM Property p WHERE p.visible = true AND p.status = 'ACTIVE' " +
            "AND (LOWER(p.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.locality) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Property> searchByQuery(@Param("query") String query, Pageable pageable);
}