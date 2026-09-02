package com.expense.tracker.dashboard.service;

import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.dashboard.dto.CategorySpendingResponse;
import com.expense.tracker.dashboard.dto.MonthlySpendingResponse;
import com.expense.tracker.dashboard.dto.SummaryResponse;
import com.expense.tracker.expense.repository.ExpenseRepository;
import com.expense.tracker.group.repository.GroupExpenseShareRepository;
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
    private final GroupExpenseShareRepository groupExpenseShareRepository;
    private final UserRepository userRepository;

    /**
     * CONCEPT: personal spending vs. group spending
     * A user's personal Expense rows are their own private spending. Group
     * expenses are different: one member pays the full amount up front, but
     * that isn't "their" spending - only their SHARE of it is (the rest is
     * money owed back to them, or by them, tracked via BalanceService).
     * So group spending here is always the sum of the user's shareAmount
     * across their group expenses, never the raw amount they paid - that
     * avoids inflating their totals with money that isn't really theirs.
     * It's reported as its own figure (not silently folded into the
     * personal total) so it stays easy to audit, plus a "combined" figure
     * for anyone who wants the simple grand total.
     */
    public SummaryResponse getSummary(String userEmail) {
        User user = getUser(userEmail);
        BigDecimal total = expenseRepository.sumAllByUserId(user.getId());
        BigDecimal groupTotal = groupExpenseShareRepository.sumShareAmountByUserId(user.getId());

        YearMonth now = YearMonth.now();
        YearMonth prev = now.minusMonths(1);

        BigDecimal current = expenseRepository.sumByUserIdAndDateRange(
                user.getId(), now.atDay(1), now.atEndOfMonth());
        BigDecimal previous = expenseRepository.sumByUserIdAndDateRange(
                user.getId(), prev.atDay(1), prev.atEndOfMonth());
        BigDecimal groupCurrent = groupExpenseShareRepository.sumShareAmountByUserIdAndDateRange(
                user.getId(), now.atDay(1), now.atEndOfMonth());

        BigDecimal changePercent = BigDecimal.ZERO;
        if (previous.compareTo(BigDecimal.ZERO) > 0) {
            changePercent = current.subtract(previous)
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new SummaryResponse(
                total,
                current,
                previous,
                changePercent,
                groupTotal,
                groupCurrent,
                total.add(groupTotal),
                current.add(groupCurrent)
        );
    }

    /**
     * The monthly chart shows COMBINED spending (personal + group share) per
     * month, matching the "Combined spending" figure on the summary cards -
     * not just personal Expense rows on their own, which used to make the
     * chart disagree with the cards whenever a month's spending was mostly
     * or entirely group expenses.
     */
    public List<MonthlySpendingResponse> getMonthly(String userEmail, int monthsBack) {
        User user = getUser(userEmail);
        YearMonth startMonth = YearMonth.now().minusMonths(monthsBack - 1L);
        YearMonth endMonth = YearMonth.now();
        LocalDate start = startMonth.atDay(1);
        LocalDate end = endMonth.atEndOfMonth();

        java.util.Map<String, BigDecimal> personalByMonth = expenseRepository
                .sumByMonthForUserInRange(user.getId(), start, end).stream()
                .collect(java.util.stream.Collectors.toMap(row -> (String) row[0], row -> (BigDecimal) row[1]));

        java.util.Map<String, BigDecimal> groupByMonth = groupExpenseShareRepository
                .sumShareAmountByMonthForUserInRange(user.getId(), start, end).stream()
                .collect(java.util.stream.Collectors.toMap(row -> (String) row[0], row -> (BigDecimal) row[1]));

        List<MonthlySpendingResponse> result = new java.util.ArrayList<>();
        for (YearMonth cursor = startMonth; !cursor.isAfter(endMonth); cursor = cursor.plusMonths(1)) {
            String key = cursor.toString(); // e.g. "2026-09", matches the %Y-%m DATE_FORMAT above
            BigDecimal personal = personalByMonth.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal group = groupByMonth.getOrDefault(key, BigDecimal.ZERO);
            result.add(new MonthlySpendingResponse(key, personal.add(group)));
        }
        return result;
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
