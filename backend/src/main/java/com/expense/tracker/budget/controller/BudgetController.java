package com.expense.tracker.budget.controller;

import com.expense.tracker.budget.dto.BudgetRequest;
import com.expense.tracker.budget.dto.BudgetResponse;
import com.expense.tracker.budget.service.BudgetService;
import com.expense.tracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getAll(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            Authentication authentication, @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Budget created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.update(authentication.getName(), id, request), "Budget updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication, @PathVariable Long id) {
        budgetService.delete(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted"));
    }
}
