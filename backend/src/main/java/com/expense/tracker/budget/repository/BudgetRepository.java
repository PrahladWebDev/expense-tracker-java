package com.expense.tracker.budget.repository;

import com.expense.tracker.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUserIdAndMonth(Long userId, String month);
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    List<Budget> findAllByUserIdOrderByMonthDesc(Long userId);
}
