package com.expense.tracker.group.service;

import com.expense.tracker.common.exception.ForbiddenException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.common.storage.FileStorageService;
import com.expense.tracker.group.dto.ExpenseShareInput;
import com.expense.tracker.group.dto.GroupExpenseRequest;
import com.expense.tracker.group.dto.GroupExpenseResponse;
import com.expense.tracker.group.entity.*;
import com.expense.tracker.group.mapper.GroupExpenseMapper;
import com.expense.tracker.group.repository.GroupExpenseRepository;
import com.expense.tracker.group.repository.GroupMemberRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupExpenseService {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.02");

    private final GroupExpenseRepository groupExpenseRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final GroupExpenseMapper mapper;
    private final GroupActivityService activityService;
    private final FileStorageService fileStorageService;

    public List<GroupExpenseResponse> listExpenses(String userEmail, Long groupId) {
        requireMembership(userEmail, groupId);
        return groupExpenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** Same as listExpenses but excludes soft-deleted ones - used for CSV/PDF exports, which are financial records. */
    public List<GroupExpenseResponse> listActiveExpenses(String userEmail, Long groupId) {
        requireMembership(userEmail, groupId);
        return groupExpenseRepository.findByGroupIdAndDeletedFalseOrderByExpenseDateDescCreatedAtDesc(groupId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public GroupExpenseResponse getExpense(String userEmail, Long groupId, Long expenseId) {
        requireMembership(userEmail, groupId);
        return mapper.toResponse(getExpenseEntity(groupId, expenseId));
    }

    @Transactional
    public GroupExpenseResponse addExpense(String userEmail, Long groupId, GroupExpenseRequest request) {
        requireMembership(userEmail, groupId);
        ExpenseGroup group = groupService.getGroupEntity(groupId);
        groupService.requireOpen(group);
        User actor = userRepository.findByEmail(userEmail).orElse(null);

        User paidBy = memberRepository.findByGroupIdAndUserId(groupId, request.paidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("paidByUserId is not a member of this group"))
                .getUser();

        Map<Long, User> participantUsers = new HashMap<>();
        for (ExpenseShareInput share : request.shares()) {
            User u = memberRepository.findByGroupIdAndUserId(groupId, share.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("A participant is not a member of this group"))
                    .getUser();
            participantUsers.put(share.userId(), u);
        }

        Map<Long, BigDecimal> computedShares = computeShares(request);

        GroupExpense expense = GroupExpense.builder()
                .group(group)
                .paidBy(paidBy)
                .amount(request.amount())
                .description(request.description())
                .expenseDate(request.expenseDate())
                .splitType(request.splitType())
                .build();

        List<GroupExpenseShare> shareEntities = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : computedShares.entrySet()) {
            shareEntities.add(GroupExpenseShare.builder()
                    .groupExpense(expense)
                    .user(participantUsers.get(entry.getKey()))
                    .shareAmount(entry.getValue())
                    .build());
        }
        expense.setShares(shareEntities);

        GroupExpense saved = groupExpenseRepository.save(expense);

        activityService.log(group, actor, GroupActivityType.EXPENSE_ADDED,
                (actor != null ? actor.getFullName() : "Someone") + " added ₹" + saved.getAmount()
                        + " for " + (saved.getDescription() != null ? saved.getDescription() : "a group expense")
                        + " (paid by " + paidBy.getFullName() + ")");

        return mapper.toResponse(saved);
    }

    /**
     * Soft delete: the expense is never removed from the group. It stays
     * visible (struck through) in the list with who deleted it, but is
     * excluded from balances and dashboard totals as if it never happened -
     * see BalanceService and GroupExpenseShareRepository's "deleted = false"
     * guards.
     */
    @Transactional
    public void deleteExpense(String userEmail, Long groupId, Long expenseId) {
        User requester = requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        groupService.requireOpen(expense.getGroup());

        if (expense.isDeleted()) {
            return; // already deleted, nothing to do
        }

        boolean isOwner = memberRepository.findByGroupIdAndUserId(groupId, requester.getId())
                .map(m -> m.getRole() == GroupMemberRole.OWNER)
                .orElse(false);
        boolean isPayer = expense.getPaidBy().getId().equals(requester.getId());
        if (!isOwner && !isPayer) {
            throw new ForbiddenException("Only the person who paid, or the group owner, can delete this expense");
        }

        expense.setDeleted(true);
        expense.setDeletedAt(java.time.Instant.now());
        expense.setDeletedBy(requester);
        groupExpenseRepository.save(expense);

        activityService.log(expense.getGroup(), requester, GroupActivityType.EXPENSE_DELETED,
                requester.getFullName() + " deleted the ₹" + expense.getAmount() + " expense \""
                        + (expense.getDescription() != null ? expense.getDescription() : "group expense") + "\"");
    }

    /** Only a member can attach a receipt photo; any existing receipt on the expense is replaced. */
    @Transactional
    public void uploadReceipt(String userEmail, Long groupId, Long expenseId, MultipartFile file) {
        requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        groupService.requireOpen(expense.getGroup());

        if (expense.getReceiptStoredName() != null) {
            fileStorageService.delete(expense.getReceiptStoredName());
        }

        String storedName = fileStorageService.store(file);
        expense.setReceiptStoredName(storedName);
        expense.setReceiptOriginalName(file.getOriginalFilename());
        expense.setReceiptContentType(file.getContentType());
        groupExpenseRepository.save(expense);
    }

    public ReceiptFile getReceipt(String userEmail, Long groupId, Long expenseId) {
        requireMembership(userEmail, groupId);
        GroupExpense expense = getExpenseEntity(groupId, expenseId);
        if (expense.getReceiptStoredName() == null) {
            throw new ResourceNotFoundException("This expense has no receipt attached");
        }
        InputStream stream = fileStorageService.read(expense.getReceiptStoredName());
        return new ReceiptFile(stream, expense.getReceiptContentType(), expense.getReceiptOriginalName());
    }

    public record ReceiptFile(InputStream stream, String contentType, String originalName) {}

    /**
     * Turns the caller's split instructions into a concrete rupee amount
     * per participant, guaranteeing the shares sum EXACTLY to the total
     * amount (no stray paise lost or gained to rounding).
     */
    private Map<Long, BigDecimal> computeShares(GroupExpenseRequest request) {
        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        List<ExpenseShareInput> shares = request.shares();
        Map<Long, BigDecimal> result = new java.util.LinkedHashMap<>();

        switch (request.splitType()) {
            case EQUAL -> {
                int n = shares.size();
                BigDecimal base = amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
                BigDecimal distributed = base.multiply(BigDecimal.valueOf(n));
                BigDecimal remainder = amount.subtract(distributed); // leftover paise, e.g. 0.02
                BigDecimal paise = new BigDecimal("0.01");
                int remainderUnits = remainder.divide(paise, 0, RoundingMode.HALF_UP).intValue();

                for (int i = 0; i < n; i++) {
                    BigDecimal share = base;
                    if (i < remainderUnits) {
                        share = share.add(paise); // spread leftover paise across the first few participants
                    }
                    result.put(shares.get(i).userId(), share);
                }
            }
            case EXACT -> {
                BigDecimal sum = BigDecimal.ZERO;
                for (ExpenseShareInput s : shares) {
                    if (s.value() == null) {
                        throw new IllegalArgumentException("Every participant needs an exact amount for an EXACT split");
                    }
                    BigDecimal value = s.value().setScale(2, RoundingMode.HALF_UP);
                    result.merge(s.userId(), value, BigDecimal::add);
                    sum = sum.add(value);
                }
                if (sum.subtract(amount).abs().compareTo(TOLERANCE) > 0) {
                    throw new IllegalArgumentException(
                            "Exact shares (" + sum + ") must add up to the expense amount (" + amount + ")");
                }
            }
            case PERCENTAGE -> {
                BigDecimal percentSum = BigDecimal.ZERO;
                for (ExpenseShareInput s : shares) {
                    if (s.value() == null) {
                        throw new IllegalArgumentException("Every participant needs a percentage for a PERCENTAGE split");
                    }
                    percentSum = percentSum.add(s.value());
                }
                if (percentSum.subtract(BigDecimal.valueOf(100)).abs().compareTo(new BigDecimal("0.5")) > 0) {
                    throw new IllegalArgumentException("Percentages must add up to 100 (got " + percentSum + ")");
                }

                BigDecimal runningTotal = BigDecimal.ZERO;
                for (int i = 0; i < shares.size(); i++) {
                    ExpenseShareInput s = shares.get(i);
                    BigDecimal share;
                    if (i == shares.size() - 1) {
                        // last participant absorbs the rounding remainder so the
                        // total always matches the expense amount exactly
                        share = amount.subtract(runningTotal).setScale(2, RoundingMode.HALF_UP);
                    } else {
                        share = amount.multiply(s.value())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        runningTotal = runningTotal.add(share);
                    }
                    result.merge(s.userId(), share, BigDecimal::add);
                }
            }
        }
        return result;
    }

    private GroupExpense getExpenseEntity(Long groupId, Long expenseId) {
        return groupExpenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group expense not found"));
    }

    private User requireMembership(String userEmail, Long groupId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        groupService.requireMembership(groupId, user.getId());
        return user;
    }
}
