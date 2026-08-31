package com.expense.tracker.dashboard.dto;

import java.math.BigDecimal;

public record CategorySpendingResponse(Long categoryId, String categoryName, String color, BigDecimal total) {}
