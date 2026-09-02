package com.expense.tracker.group.mapper;

import com.expense.tracker.group.dto.ExpenseShareResponse;
import com.expense.tracker.group.dto.GroupExpenseResponse;
import com.expense.tracker.group.entity.GroupExpense;
import org.springframework.stereotype.Component;

@Component
public class GroupExpenseMapper {

    public GroupExpenseResponse toResponse(GroupExpense e) {
        return new GroupExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDescription(),
                e.getExpenseDate(),
                e.getPaidBy().getId(),
                e.getPaidBy().getFullName(),
                e.getSplitType().name(),
                e.getShares().stream()
                        .map(s -> new ExpenseShareResponse(s.getUser().getId(), s.getUser().getFullName(), s.getShareAmount()))
                        .toList(),
                e.getReceiptStoredName() != null,
                e.getReceiptOriginalName(),
                e.getCreatedAt()
        );
    }
}
