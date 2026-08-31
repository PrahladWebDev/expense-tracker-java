package com.expense.tracker.dashboard.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.dashboard.dto.CategorySpendingResponse;
import com.expense.tracker.dashboard.dto.MonthlySpendingResponse;
import com.expense.tracker.dashboard.dto.SummaryResponse;
import com.expense.tracker.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> summary(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(authentication.getName())));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlySpendingResponse>>> monthly(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int monthsBack) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthly(authentication.getName(), monthsBack)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySpendingResponse>>> categories(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCategoryBreakdown(authentication.getName(), from, to)));
    }
}
