package com.expense.tracker.group.mapper;

import com.expense.tracker.group.dto.GroupMemberResponse;
import com.expense.tracker.group.dto.GroupResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.group.entity.GroupMember;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {

    public GroupMemberResponse toMemberResponse(GroupMember m) {
        return new GroupMemberResponse(
                m.getUser().getId(),
                m.getUser().getFullName(),
                m.getUser().getEmail(),
                m.getRole().name(),
                m.getJoinedAt()
        );
    }

    public GroupResponse toResponse(ExpenseGroup g, List<GroupMember> members) {
        return new GroupResponse(
                g.getId(),
                g.getName(),
                g.getDescription(),
                g.getCreatedBy().getId(),
                g.getCreatedBy().getFullName(),
                g.getCreatedAt(),
                g.getStatus().name(),
                g.getClosedAt(),
                members.stream().map(this::toMemberResponse).toList()
        );
    }
}
