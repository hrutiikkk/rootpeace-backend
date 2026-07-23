package com.checkspace.backend.repository;

import com.checkspace.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal sumSuccessfulPaymentsBetween(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    // IDEMPOTENCY CHECK — find existing payment by key
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // User payment history
    java.util.List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
}