package com.checkspace.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long listingsToday;
    private long listingsThisMonth;
    private long dealsClosedToday;
    private long dealsClosedThisMonth;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    private long pendingVerifications;
    private long activeListings;
}