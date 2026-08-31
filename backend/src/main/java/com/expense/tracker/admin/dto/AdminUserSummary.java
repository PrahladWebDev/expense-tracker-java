package com.expense.tracker.admin.dto;

import java.time.Instant;

public record AdminUserSummary(Long id, String fullName, String email, String role, Instant createdAt) {}
