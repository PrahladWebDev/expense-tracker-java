package com.expense.tracker.dashboard.dto;

import java.math.BigDecimal;

public record MonthlySpendingResponse(String month, BigDecimal total) {}
