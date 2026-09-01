package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupExpenseShareRepository extends JpaRepository<GroupExpenseShare, Long> {
    List<GroupExpenseShare> findByGroupExpense_Group_Id(Long groupId);
    List<GroupExpenseShare> findByGroupExpense_Group_IdAndUser_Id(Long groupId, Long userId);
}
