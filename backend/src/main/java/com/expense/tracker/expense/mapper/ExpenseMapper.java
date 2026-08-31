package com.expense.tracker.expense.mapper;

import com.expense.tracker.expense.dto.ExpenseResponse;
import com.expense.tracker.expense.entity.Expense;
import org.springframework.stereotype.Component;

/**
 * CONCEPT: Mapper
 * A small, single-purpose class whose only job is converting between an
 * Entity and its DTO(s). Keeping this logic out of the Service class keeps
 * each class focused on one responsibility (Single Responsibility
 * Principle) and makes the conversion logic independently testable/reusable
 * (e.g. the dashboard service can reuse this too).
 */
@Component
public class ExpenseMapper {
    public ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDescription(),
                e.getExpenseDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getCategory().getColor(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
