package com.checkspace.backend.repository;

import com.checkspace.backend.model.PropertyBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyBlacklistRepository extends JpaRepository<PropertyBlacklist, Long> {
    // This custom method makes the 'existsByAddressHash' red line go away!
    boolean existsByAddressHash(String addressHash);
}