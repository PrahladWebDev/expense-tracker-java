package com.expense.tracker.category.dto;

import java.time.Instant;

public record CategoryResponse(Long id, String name, String color, Instant createdAt) {}
