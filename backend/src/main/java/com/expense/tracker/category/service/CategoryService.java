package com.expense.tracker.category.service;

import com.expense.tracker.category.dto.CategoryRequest;
import com.expense.tracker.category.dto.CategoryResponse;
import com.expense.tracker.category.entity.Category;
import com.expense.tracker.category.repository.CategoryRepository;
import com.expense.tracker.common.exception.DuplicateResourceException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CONCEPT: Streams and Optional
 * `.stream().map(...).toList()` (CONCEPT: Streams) is a functional-style
 * pipeline over a Collection: transform each Category entity into a
 * CategoryResponse DTO without a manual for-loop and mutable ArrayList.
 * `.orElseThrow(...)` (CONCEPT: Optional) avoids null checks: repository
 * methods that might not find a row return Optional<Category> instead of a
 * possibly-null Category, forcing the caller to explicitly handle "absent".
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAll(String userEmail) {
        User user = getUser(userEmail);
        return categoryRepository.findAllByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(String userEmail, CategoryRequest request) {
        User user = getUser(userEmail);
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.name())) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        Category category = Category.builder()
                .name(request.name())
                .color(request.color())
                .user(user)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(String userEmail, Long id, CategoryRequest request) {
        User user = getUser(userEmail);
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.name());
        category.setColor(request.color());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(String userEmail, Long id) {
        User user = getUser(userEmail);
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getColor(), c.getCreatedAt());
    }
}
