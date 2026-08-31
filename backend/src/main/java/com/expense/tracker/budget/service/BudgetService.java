package com.expense.tracker.budget.service;

import com.expense.tracker.budget.dto.BudgetRequest;
import com.expense.tracker.budget.dto.BudgetResponse;
import com.expense.tracker.budget.entity.Budget;
import com.expense.tracker.budget.repository.BudgetRepository;
import com.expense.tracker.category.entity.Category;
import com.expense.tracker.category.repository.CategoryRepository;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.expense.repository.ExpenseRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public List<BudgetResponse> getAll(String userEmail) {
        User user = getUser(userEmail);
        return budgetRepository.findAllByUserIdOrderByMonthDesc(user.getId())
                .stream()
                .map(b -> withUsage(user, b))
                .toList();
    }

    @Transactional
    public BudgetResponse create(String userEmail, BudgetRequest request) {
        User user = getUser(userEmail);
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        Budget budget = Budget.builder()
                .amount(request.amount())
                .month(request.month())
                .category(category)
                .user(user)
                .build();
        return withUsage(user, budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(String userEmail, Long id, BudgetRequest request) {
        User user = getUser(userEmail);
        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUserId(request.categoryId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setCategory(category);
        return withUsage(user, budgetRepository.save(budget));
    }

    @Transactional
    public void delete(String userEmail, Long id) {
        User user = getUser(userEmail);
        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        budgetRepository.delete(budget);
    }

    /** Computes how much has actually been spent against a budget's month (and category, if scoped). */
    private BudgetResponse withUsage(User user, Budget budget) {
        YearMonth ym = YearMonth.parse(budget.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent;
        if (budget.getCategory() != null) {
            spent = expenseRepository.sumByCategoryForUserInRange(user.getId(), start, end).stream()
                    .filter(row -> budget.getCategory().getId().equals(row[0]))
                    .map(row -> (BigDecimal) row[3])
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        } else {
            spent = expenseRepository.sumByUserIdAndDateRange(user.getId(), start, end);
        }

        BigDecimal remaining = budget.getAmount().subtract(spent);
        double percentUsed = budget.getAmount().compareTo(BigDecimal.ZERO) == 0
                ? 0
                : spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100;

        return new BudgetResponse(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getCategory() != null ? budget.getCategory().getName() : "Overall",
                spent,
                remaining,
                Math.round(percentUsed * 10.0) / 10.0
        );
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
