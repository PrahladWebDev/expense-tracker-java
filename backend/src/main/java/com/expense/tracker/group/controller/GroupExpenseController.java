package com.expense.tracker.group.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.group.dto.GroupExpenseRequest;
import com.expense.tracker.group.dto.GroupExpenseResponse;
import com.expense.tracker.group.service.GroupExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class GroupExpenseController {

    private final GroupExpenseService groupExpenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupExpenseResponse>>> list(
            Authentication authentication, @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupExpenseService.listExpenses(authentication.getName(), groupId)));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<GroupExpenseResponse>> getOne(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long expenseId) {
        return ResponseEntity.ok(ApiResponse.success(groupExpenseService.getExpense(authentication.getName(), groupId, expenseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupExpenseResponse>> create(
            Authentication authentication, @PathVariable Long groupId, @Valid @RequestBody GroupExpenseRequest request) {
        GroupExpenseResponse response = groupExpenseService.addExpense(authentication.getName(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Group expense added"));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication, @PathVariable Long groupId, @PathVariable Long expenseId) {
        groupExpenseService.deleteExpense(authentication.getName(), groupId, expenseId);
        return ResponseEntity.ok(ApiResponse.success(null, "Group expense deleted"));
    }
}
