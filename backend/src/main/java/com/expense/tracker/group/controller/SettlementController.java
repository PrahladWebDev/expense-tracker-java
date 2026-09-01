package com.expense.tracker.group.controller;

import com.expense.tracker.common.response.ApiResponse;
import com.expense.tracker.group.dto.SettlementRequest;
import com.expense.tracker.group.dto.SettlementResponse;
import com.expense.tracker.group.dto.SettlementSuggestionResponse;
import com.expense.tracker.group.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> list(
            Authentication authentication, @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.listSettlements(authentication.getName(), groupId)));
    }

    /** "Who should pay whom, how much" - the minimal set of payments to zero out every balance. */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SettlementSuggestionResponse>>> suggestions(
            Authentication authentication, @PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.getSuggestions(authentication.getName(), groupId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SettlementResponse>> record(
            Authentication authentication, @PathVariable Long groupId, @Valid @RequestBody SettlementRequest request) {
        SettlementResponse response = settlementService.recordSettlement(authentication.getName(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Settlement recorded"));
    }
}
