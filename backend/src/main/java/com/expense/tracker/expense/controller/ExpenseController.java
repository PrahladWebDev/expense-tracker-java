package com.expense.tracker.expense.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.common.response.PageResponse;
import com.expense.tracker.expense.dto.ExpenseRequest;
import com.expense.tracker.expense.dto.ExpenseResponse;
import com.expense.tracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * GET /api/v1/expenses supports optional query params for search, filter,
 * sort and pagination all at once, e.g.:
 *   /api/v1/expenses?search=coffee&categoryId=3&from=2026-01-01&to=2026-01-31
 *     &minAmount=5&maxAmount=100&page=0&size=20&sortBy=amount&direction=desc
 * Every param is optional (required = false) - ExpenseSpecifications only
 * applies the ones that were actually sent.
 */
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> search(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResponse<ExpenseResponse> result = expenseService.search(
                authentication.getName(), search, categoryId, from, to, minAmount, maxAmount,
                page, size, sortBy, direction
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getById(authentication.getName(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(
            Authentication authentication, @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Expense created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.update(authentication.getName(), id, request), "Expense updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication, @PathVariable Long id) {
        expenseService.delete(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted"));
    }
}
