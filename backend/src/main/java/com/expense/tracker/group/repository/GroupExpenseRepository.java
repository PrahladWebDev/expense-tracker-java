package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupExpenseRepository extends JpaRepository<GroupExpense, Long> {
    List<GroupExpense> findByGroupIdOrderByExpenseDateDescCreatedAtDesc(Long groupId);

    /** Excludes soft-deleted expenses - use this wherever a total/balance/export must not count a deleted expense. */
    List<GroupExpense> findByGroupIdAndDeletedFalseOrderByExpenseDateDescCreatedAtDesc(Long groupId);

    Optional<GroupExpense> findByIdAndGroupId(Long id, Long groupId);
}
