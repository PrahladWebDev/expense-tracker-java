package com.expense.tracker.category.controller;

import com.expense.tracker.category.dto.CategoryRequest;
import com.expense.tracker.category.dto.CategoryResponse;
import com.expense.tracker.category.service.CategoryService;
import com.expense.tracker.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll(authentication.getName())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            Authentication authentication, @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Category created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            Authentication authentication, @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(authentication.getName(), id, request), "Category updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(Authentication authentication, @PathVariable Long id) {
        categoryService.delete(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted"));
    }
}
