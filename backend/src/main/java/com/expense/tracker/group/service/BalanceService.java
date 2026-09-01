package com.expense.tracker.group.service;

import com.expense.tracker.group.dto.MemberBalanceResponse;
import com.expense.tracker.group.dto.SettlementSuggestionResponse;
import com.expense.tracker.group.entity.GroupExpense;
import com.expense.tracker.group.entity.GroupExpenseShare;
import com.expense.tracker.group.entity.GroupMember;
import com.expense.tracker.group.entity.Settlement;
import com.expense.tracker.group.repository.GroupExpenseRepository;
import com.expense.tracker.group.repository.GroupMemberRepository;
import com.expense.tracker.group.repository.SettlementRepository;
import com.expense.tracker.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Computes "who owes whom" for a group, and a minimal set of payments that
 * would settle every balance to zero (debt simplification).
 *
 * CONCEPT: net balance
 * For each member: netBalance = (total they paid across group expenses)
 *                              - (total of their own shares across group expenses)
 *                              + (settlements they paid out) - (settlements they received)
 * Positive => the group owes them money. Negative => they owe the group.
 * Because every expense's shares always sum to that expense's amount, the
 * sum of all members' netBalance is always exactly zero before any
 * settlement, and stays zero after (a settlement just moves the same
 * amount from one member's balance to another's).
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    private final GroupMemberRepository memberRepository;
    private final GroupExpenseRepository groupExpenseRepository;
    private final SettlementRepository settlementRepository;

    public List<MemberBalanceResponse> computeBalances(Long groupId) {
        List<GroupMember> members = memberRepository.findByGroupId(groupId);

        Map<Long, User> usersById = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalPaid = new LinkedHashMap<>();
        Map<Long, BigDecimal> totalShare = new LinkedHashMap<>();
        Map<Long, BigDecimal> net = new LinkedHashMap<>();

        for (GroupMember m : members) {
            Long uid = m.getUser().getId();
            usersById.put(uid, m.getUser());
            totalPaid.put(uid, BigDecimal.ZERO);
            totalShare.put(uid, BigDecimal.ZERO);
            net.put(uid, BigDecimal.ZERO);
        }

        List<GroupExpense> expenses = groupExpenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId);
        for (GroupExpense e : expenses) {
            Long payerId = e.getPaidBy().getId();
            totalPaid.merge(payerId, e.getAmount(), BigDecimal::add);
            net.merge(payerId, e.getAmount(), BigDecimal::add);
            for (GroupExpenseShare s : e.getShares()) {
                Long uid = s.getUser().getId();
                totalShare.merge(uid, s.getShareAmount(), BigDecimal::add);
                net.merge(uid, s.getShareAmount().negate(), BigDecimal::add);
            }
        }

        for (Settlement s : settlementRepository.findByGroupIdOrderBySettledAtDesc(groupId)) {
            net.merge(s.getFromUser().getId(), s.getAmount(), BigDecimal::add);
            net.merge(s.getToUser().getId(), s.getAmount().negate(), BigDecimal::add);
        }

        List<MemberBalanceResponse> result = new ArrayList<>();
        for (GroupMember m : members) {
            Long uid = m.getUser().getId();
            result.add(new MemberBalanceResponse(
                    uid,
                    usersById.get(uid).getFullName(),
                    totalPaid.get(uid).setScale(2, RoundingMode.HALF_UP),
                    totalShare.get(uid).setScale(2, RoundingMode.HALF_UP),
                    net.get(uid).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        return result;
    }

    /**
     * CONCEPT: Debt simplification (greedy min-cash-flow)
     * Rather than suggesting every individual expense's split as a separate
     * payment, we net everyone's balance down to a single number, then
     * greedily match the largest creditor against the largest debtor,
     * repeating until every balance is (near) zero. This minimizes the
     * number of payments needed to settle the whole group.
     */
    public List<SettlementSuggestionResponse> suggestSettlements(Long groupId) {
        List<MemberBalanceResponse> balances = computeBalances(groupId);

        PriorityQueue<BalanceEntry> creditors = new PriorityQueue<>((a, b) -> b.amount.compareTo(a.amount));
        PriorityQueue<BalanceEntry> debtors = new PriorityQueue<>((a, b) -> b.amount.compareTo(a.amount));

        for (MemberBalanceResponse b : balances) {
            if (b.netBalance().compareTo(EPSILON) > 0) {
                creditors.add(new BalanceEntry(b.userId(), b.fullName(), b.netBalance()));
            } else if (b.netBalance().negate().compareTo(EPSILON) > 0) {
                debtors.add(new BalanceEntry(b.userId(), b.fullName(), b.netBalance().negate()));
            }
        }

        List<SettlementSuggestionResponse> suggestions = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            BalanceEntry topCreditor = creditors.poll();
            BalanceEntry topDebtor = debtors.poll();

            BigDecimal settleAmount = topCreditor.amount.min(topDebtor.amount).setScale(2, RoundingMode.HALF_UP);
            suggestions.add(new SettlementSuggestionResponse(
                    topDebtor.userId, topDebtor.name, topCreditor.userId, topCreditor.name, settleAmount
            ));

            BigDecimal creditorRemaining = topCreditor.amount.subtract(settleAmount);
            BigDecimal debtorRemaining = topDebtor.amount.subtract(settleAmount);

            if (creditorRemaining.compareTo(EPSILON) > 0) {
                creditors.add(new BalanceEntry(topCreditor.userId, topCreditor.name, creditorRemaining));
            }
            if (debtorRemaining.compareTo(EPSILON) > 0) {
                debtors.add(new BalanceEntry(topDebtor.userId, topDebtor.name, debtorRemaining));
            }
        }
        return suggestions;
    }

    private record BalanceEntry(Long userId, String name, BigDecimal amount) {}
}
