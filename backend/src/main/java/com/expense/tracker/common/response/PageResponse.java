package com.expense.tracker.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wraps Spring Data's Page<T> into a simpler shape for the frontend.
 * We don't return Spring's raw Page object directly because it serializes
 * internal implementation details (Pageable, Sort objects) that couple our
 * public API contract to a specific Spring Data version.
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
