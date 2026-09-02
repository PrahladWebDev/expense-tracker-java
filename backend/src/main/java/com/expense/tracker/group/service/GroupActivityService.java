package com.expense.tracker.group.service;

import com.expense.tracker.group.dto.GroupActivityResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.group.entity.GroupActivity;
import com.expense.tracker.group.entity.GroupActivityType;
import com.expense.tracker.group.repository.GroupActivityRepository;
import com.expense.tracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Writes an entry every time something notable happens in a group, and
 * serves the resulting feed. Other services (GroupService,
 * GroupExpenseService, SettlementService, GroupExpenseCommentService) call
 * `log(...)` right after their own mutation succeeds - this class never
 * decides WHETHER something happened, only records it.
 */
@Service
@RequiredArgsConstructor
public class GroupActivityService {

    private static final int FEED_LIMIT = 100;

    private final GroupActivityRepository repository;

    @Transactional
    public void log(ExpenseGroup group, User actor, GroupActivityType type, String message) {
        GroupActivity activity = GroupActivity.builder()
                .group(group)
                .actor(actor)
                .type(type)
                .message(message)
                .build();
        repository.save(activity);
    }

    public List<GroupActivityResponse> listActivity(Long groupId) {
        return repository.findByGroupIdOrderByCreatedAtDesc(groupId, PageRequest.of(0, FEED_LIMIT)).stream()
                .map(a -> new GroupActivityResponse(
                        a.getId(),
                        a.getType().name(),
                        a.getMessage(),
                        a.getActor() != null ? a.getActor().getId() : null,
                        a.getActor() != null ? a.getActor().getFullName() : "Someone",
                        a.getCreatedAt()))
                .toList();
    }
}
