package com.expense.tracker.expense.repository;

import com.expense.tracker.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CONCEPT: JpaSpecificationExecutor
 * Derived query methods (findByXAndY) work great for FIXED combinations of
 * filters. But expenses need OPTIONAL, combinable filters (search text,
 * category, date range, min/max amount - any subset of which might be
 * present). Writing a derived method for every combination would explode
 * combinatorially. JpaSpecificationExecutor lets us build a query
 * DYNAMICALLY at runtime (see ExpenseSpecifications) - only adding a WHERE
 * clause for filters that are actually present.
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findAllByUserIdAndExpenseDateBetween(Long userId, LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId"
    )
    BigDecimal sumAllByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end"
    )
    BigDecimal sumByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query(
        "SELECT e.category.id, e.category.name, e.category.color, COALESCE(SUM(e.amount), 0) " +
        "FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end " +
        "GROUP BY e.category.id, e.category.name, e.category.color"
    )
    List<Object[]> sumByCategoryForUserInRange(Long userId, LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query(
        "SELECT FUNCTION('DATE_FORMAT', e.expenseDate, '%Y-%m') as ym, COALESCE(SUM(e.amount), 0) " +
        "FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end " +
        "GROUP BY ym ORDER BY ym"
    )
    List<Object[]> sumByMonthForUserInRange(Long userId, LocalDate start, LocalDate end);
}
