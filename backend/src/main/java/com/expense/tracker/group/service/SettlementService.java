package com.expense.tracker.group.service;

import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.group.dto.SettlementRequest;
import com.expense.tracker.group.dto.SettlementResponse;
import com.expense.tracker.group.dto.SettlementSuggestionResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.group.entity.Settlement;
import com.expense.tracker.group.mapper.SettlementMapper;
import com.expense.tracker.group.repository.SettlementRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final BalanceService balanceService;
    private final SettlementMapper mapper;

    public List<SettlementResponse> listSettlements(String userEmail, Long groupId) {
        requireMembership(userEmail, groupId);
        return settlementRepository.findByGroupIdOrderBySettledAtDesc(groupId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<SettlementSuggestionResponse> getSuggestions(String userEmail, Long groupId) {
        requireMembership(userEmail, groupId);
        return balanceService.suggestSettlements(groupId);
    }

    /**
     * Records that fromUser paid toUser `amount` outside the app (cash,
     * UPI, bank transfer...) to settle part or all of their group debt.
     * Any member can log a settlement - e.g. the group owner reconciling
     * cash payments made between two other members.
     */
    @Transactional
    public SettlementResponse recordSettlement(String userEmail, Long groupId, SettlementRequest request) {
        requireMembership(userEmail, groupId);
        ExpenseGroup group = groupService.getGroupEntity(groupId);

        groupService.requireMembership(groupId, request.fromUserId());
        groupService.requireMembership(groupId, request.toUserId());

        if (request.fromUserId().equals(request.toUserId())) {
            throw new IllegalArgumentException("fromUserId and toUserId must be different members");
        }

        User fromUser = getUser(request.fromUserId());
        User toUser = getUser(request.toUserId());

        Settlement settlement = Settlement.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(request.amount())
                .note(request.note())
                .build();

        return mapper.toResponse(settlementRepository.save(settlement));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void requireMembership(String userEmail, Long groupId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        groupService.requireMembership(groupId, user.getId());
    }
}
