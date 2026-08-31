package com.expense.tracker.user.dto;

import java.time.Instant;

public record ProfileResponse(Long id, String fullName, String email, String role, Instant createdAt) {}
