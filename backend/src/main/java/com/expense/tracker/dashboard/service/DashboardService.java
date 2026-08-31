package com.expense.tracker.dashboard.service;

import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.dashboard.dto.CategorySpendingResponse;
import com.expense.tracker.dashboard.dto.MonthlySpendingResponse;
import com.expense.tracker.dashboard.dto.SummaryResponse;
import com.expense.tracker.expense.repository.ExpenseRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public SummaryResponse getSummary(String userEmail) {
        User user = getUser(userEmail);
        BigDecimal total = expenseRepository.sumAllByUserId(user.getId());

        YearMonth now = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        BigDecimal current = expenseRepository.sumByUserIdAndDateRange(
                user.getId(), now.atDay(1), now.atEndOfMonth());
        BigDecimal previous = expenseRepository.sumByUserIdAndDateRange(
                user.getId(), prev.atDay(1), prev.atEndOfMonth());

        BigDecimal changePercent = BigDecimal.ZERO;
        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new SummaryResponse(total, current, previous, changePercent);
    }

    public List<MonthlySpendingResponse> getMonthly(String userEmail, int monthsBack) {
        User user = getUser(userEmail);
        LocalDate start = YearMonth.now().minusMonths(monthsBack - 1L).atDay(1);
        LocalDate end = YearMonth.now().atEndOfMonth();

        return expenseRepository.sumByMonthForUserInRange(user.getId(), start, end).stream()
                .map(row -> new MonthlySpendingResponse((String) row[0], (BigDecimal) row[1]))
                .toList();
    }

    public List<CategorySpendingResponse> getCategoryBreakdown(String userEmail, LocalDate from, LocalDate to) {
        User user = getUser(userEmail);
        LocalDate rangeStart = from != null ? from : YearMonth.now().atDay(1);
        LocalDate rangeEnd = to != null ? to : YearMonth.now().atEndOfMonth();

        return expenseRepository.sumByCategoryForUserInRange(user.getId(), rangeStart, rangeEnd).stream()
                .map(row -> new CategorySpendingResponse(
                        (Long) row[0], (String) row[1], (String) row[2], (BigDecimal) row[3]))
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
