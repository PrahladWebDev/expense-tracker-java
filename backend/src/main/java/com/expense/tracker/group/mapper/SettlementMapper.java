package com.expense.tracker.group.mapper;

import com.expense.tracker.group.dto.SettlementResponse;
import com.expense.tracker.group.entity.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toResponse(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getFromUser().getId(),
                s.getFromUser().getFullName(),
                s.getToUser().getId(),
                s.getToUser().getFullName(),
                s.getAmount(),
                s.getNote(),
                s.getSettledAt()
        );
    }
}
