package com.expense.tracker.expense.service;

import com.expense.tracker.category.entity.Category;
import com.expense.tracker.category.repository.CategoryRepository;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.common.response.PageResponse;
import com.expense.tracker.expense.dto.ExpenseRequest;
import com.expense.tracker.expense.dto.ExpenseResponse;
import com.expense.tracker.expense.entity.Expense;
import com.expense.tracker.expense.mapper.ExpenseMapper;
import com.expense.tracker.expense.repository.ExpenseRepository;
import com.expense.tracker.expense.specification.ExpenseSpecifications;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper mapper;

    /**
     * CONCEPT: Pagination
     * Returning ALL of a user's expenses in one response doesn't scale - a
     * user with 10,000 expenses would blow up response size and render
     * time. Pageable (page number + page size + sort) tells the database to
     * fetch only ONE page via SQL LIMIT/OFFSET, so cost is bounded
     * regardless of total row count.
     *
     * CONCEPT: Generics + varargs-like combination
     * `Specification.where(...).and(...)` chains together only the
     * non-null filter Specifications built in ExpenseSpecifications -
     * this is how we support any COMBINATION of optional filters with one
     * method instead of writing dozens of repository methods.
     */
    public PageResponse<ExpenseResponse> search(
            String userEmail, String search, Long categoryId,
            LocalDate from, LocalDate to, BigDecimal minAmount, BigDecimal maxAmount,
            int page, int size, String sortBy, String direction
    ) {
        User user = getUser(userEmail);

        List<Specification<Expense>> specs = new ArrayList<>();
        specs.add(ExpenseSpecifications.belongsToUser(user.getId()));
        specs.add(ExpenseSpecifications.descriptionContains(search));
        specs.add(ExpenseSpecifications.hasCategory(categoryId));
        specs.add(ExpenseSpecifications.dateFrom(from));
        specs.add(ExpenseSpecifications.dateTo(to));
        specs.add(ExpenseSpecifications.amountMin(minAmount));
        specs.add(ExpenseSpecifications.amountMax(maxAmount));
        specs.removeIf(s -> s == null); // drop filters that weren't provided

        Specification<Expense> finalSpec = Specification.allOf(specs);

        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = switch (sortBy == null ? "" : sortBy) {
            case "amount" -> "amount";
            case "description" -> "description";
            case "createdAt" -> "createdAt";
            default -> "expenseDate";
        };

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(dir, sortField));
        Page<Expense> result = expenseRepository.findAll(finalSpec, pageRequest);
        return PageResponse.from(result.map(mapper::toResponse));
    }

    public ExpenseResponse getById(String userEmail, Long id) {
        User user = getUser(userEmail);
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return mapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse create(String userEmail, ExpenseRequest request) {
        User user = getUser(userEmail);
        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Expense expense = Expense.builder()
                .amount(request.amount())
                .description(request.description())
                .expenseDate(request.expenseDate())
                .category(category)
                .user(user)
                .build();

        return mapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(String userEmail, Long id, ExpenseRequest request) {
        User user = getUser(userEmail);
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        Category category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        expense.setAmount(request.amount());
        expense.setDescription(request.description());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(category);

        return mapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(String userEmail, Long id) {
        User user = getUser(userEmail);
        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
