package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupExpenseShareRepository extends JpaRepository<GroupExpenseShare, Long> {
    List<GroupExpenseShare> findByGroupExpense_Group_Id(Long groupId);
    List<GroupExpenseShare> findByGroupExpense_Group_IdAndUser_Id(Long groupId, Long userId);

    /**
     * A user's true "spend" from group expenses is their SHARE of each
     * expense, not what they happened to pay - see DashboardService for why.
     * Summed across every group the user belongs to, all time.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(s.shareAmount), 0) FROM GroupExpenseShare s WHERE s.user.id = :userId"
    )
    java.math.BigDecimal sumShareAmountByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);

    /** Same as above, restricted to expenses dated within [start, end]. */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(s.shareAmount), 0) FROM GroupExpenseShare s " +
        "WHERE s.user.id = :userId AND s.groupExpense.expenseDate BETWEEN :start AND :end"
    )
    java.math.BigDecimal sumShareAmountByUserIdAndDateRange(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("start") java.time.LocalDate start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDate end);
}
