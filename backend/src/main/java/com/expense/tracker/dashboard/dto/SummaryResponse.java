package com.expense.tracker.dashboard.dto;

import java.math.BigDecimal;

public record SummaryResponse(
        BigDecimal totalAllTime,
        BigDecimal currentMonth,
        BigDecimal previousMonth,
        BigDecimal changePercent,
        BigDecimal groupSpendingAllTime,
        BigDecimal groupSpendingCurrentMonth,
        BigDecimal combinedAllTime,
        BigDecimal combinedCurrentMonth
) {}
