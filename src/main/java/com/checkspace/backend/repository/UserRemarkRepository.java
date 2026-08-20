package com.checkspace.backend.repository;

import com.checkspace.backend.model.UserRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRemarkRepository extends JpaRepository<UserRemark, Long> {
    // This custom method makes the 'existsByUserIdAndType' red line go away!
    boolean existsByUserIdAndType(Long userId, UserRemark.RemarkType type);
}