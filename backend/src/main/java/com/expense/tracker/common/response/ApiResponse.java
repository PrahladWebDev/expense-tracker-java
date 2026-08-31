package com.expense.tracker.common.response;

import java.time.Instant;

/**
 * CONCEPT: Java Record
 * A record is a compact way to declare an immutable data-carrier class.
 * `record ApiResponse<T>(boolean success, String message, T data, Instant timestamp)`
 * auto-generates a constructor, accessor methods (success(), message()...),
 * equals(), hashCode() and toString() - no boilerplate needed.
 *
 * WHY: Every API response should have the same predictable shape so the
 * React frontend can handle success/error uniformly:
 *   { success, message, data, timestamp }
 *
 * <T> is a GENERIC type parameter - it lets this one class wrap ANY type of
 * data (a User, a List<Expense>, a PageResponse...) while staying type-safe.
 */
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "OK");
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
