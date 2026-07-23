package com.checkspace.backend.repository;

import com.checkspace.backend.model.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, Long> {
    List<PropertyMedia> findByPropertyId(Long propertyId);
}