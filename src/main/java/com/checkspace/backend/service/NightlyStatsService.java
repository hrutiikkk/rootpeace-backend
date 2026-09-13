package com.checkspace.backend.service;

import com.checkspace.backend.repository.PaymentRepository;
import com.checkspace.backend.repository.PropertyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NightlyStatsService {

    private final PropertyRepository propertyRepo;
    private final PaymentRepository paymentRepo;
    private final EmailService emailService; // Ensure EmailService is also in the service package

    public NightlyStatsService(PropertyRepository propertyRepo, PaymentRepository paymentRepo, EmailService emailService) {
        this.propertyRepo = propertyRepo;
        this.paymentRepo = paymentRepo;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void generateAndSendDailyDashboard() {
        long listings = propertyRepo.countListingsToday();
        BigDecimal revenue = paymentRepo.sumRevenueToday();
        long closedDeals = propertyRepo.countDealsClosedToday();
        long pending = propertyRepo.countPendingVerifications();
        long active = propertyRepo.countActiveListings();

        String emailBody = String.format("""
            Nightly Stats Dashboard:
            - Listings Today: %d
            - Revenue Today: ₹%s
            - Deals Closed: %d
            - Pending Verifications: %d
            - Active Listings: %d
            """, listings, revenue, closedDeals, pending, active);

        emailService.sendAdminEmail("Nightly RootPeace Stats", emailBody);
    }
}