package com.expense.tracker.group.service;

import com.expense.tracker.common.exception.DuplicateResourceException;
import com.expense.tracker.common.exception.ForbiddenException;
import com.expense.tracker.common.exception.ResourceNotFoundException;
import com.expense.tracker.group.dto.AddMemberRequest;
import com.expense.tracker.group.dto.GroupRequest;
import com.expense.tracker.group.dto.GroupResponse;
import com.expense.tracker.group.entity.ExpenseGroup;
import com.expense.tracker.group.entity.GroupActivityType;
import com.expense.tracker.group.entity.GroupMember;
import com.expense.tracker.group.entity.GroupMemberRole;
import com.expense.tracker.group.entity.GroupStatus;
import com.expense.tracker.group.mapper.GroupMapper;
import com.expense.tracker.group.repository.ExpenseGroupRepository;
import com.expense.tracker.group.repository.GroupExpenseRepository;
import com.expense.tracker.group.repository.GroupMemberRepository;
import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Groups are the shared "pot" that group expenses, balances and
 * settlements are all scoped to. Every mutating operation here first
 * confirms the caller is a member (and, for owner-only actions, that
 * they're the OWNER) - group data is only visible to its members.
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupExpenseRepository groupExpenseRepository;
    private final UserRepository userRepository;
    private final GroupMapper mapper;
    private final BalanceService balanceService;
    private final GroupActivityService activityService;

    @Transactional
    public GroupResponse createGroup(String userEmail, GroupRequest request) {
        User creator = getUser(userEmail);

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(creator)
                .build();
        group = groupRepository.save(group);

        GroupMember ownerMembership = GroupMember.builder()
                .group(group)
                .user(creator)
                .role(GroupMemberRole.OWNER)
                .build();
        memberRepository.save(ownerMembership);

        activityService.log(group, creator, GroupActivityType.GROUP_CREATED, creator.getFullName() + " created the group");

        return mapper.toResponse(group, List.of(ownerMembership));
    }

    public List<GroupResponse> listMyGroups(String userEmail) {
        User user = getUser(userEmail);
        return groupRepository.findAllForUser(user.getId()).stream()
                .map(g -> mapper.toResponse(g, memberRepository.findByGroupId(g.getId())))
                .toList();
    }

    public GroupResponse getGroup(String userEmail, Long groupId) {
        User user = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        requireMembership(groupId, user.getId());
        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    @Transactional
    public GroupResponse addMember(String userEmail, Long groupId, AddMemberRequest request) {
        User requester = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        requireMembership(groupId, requester.getId());
        requireOpen(group);

        User toAdd = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("No user is registered with that email"));

        if (memberRepository.existsByGroupIdAndUserId(groupId, toAdd.getId())) {
            throw new DuplicateResourceException("That user is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(toAdd)
                .role(GroupMemberRole.MEMBER)
                .build();
        memberRepository.save(member);

        activityService.log(group, requester, GroupActivityType.MEMBER_ADDED,
                requester.getFullName() + " added " + toAdd.getFullName() + " to the group");

        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    /**
     * Anyone with a valid, current invite code can join without needing to
     * already be a known contact of an existing member - handy for friends
     * who aren't in your address book. The code is scoped to one group and
     * is invalidated (a fresh one generated) whenever the owner calls
     * regenerateInviteCode, e.g. after sharing it too widely.
     */
    @Transactional
    public GroupResponse joinByInviteCode(String userEmail, String inviteCode) {
        User user = getUser(userEmail);
        ExpenseGroup group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("This invite link is invalid or has expired"));
        requireOpen(group);

        if (memberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new DuplicateResourceException("You're already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .build();
        memberRepository.save(member);

        activityService.log(group, user, GroupActivityType.MEMBER_ADDED, user.getFullName() + " joined via invite link");

        return mapper.toResponse(group, memberRepository.findByGroupId(group.getId()));
    }

    /** Owner-only: invalidates the old invite link and issues a new one. */
    @Transactional
    public GroupResponse regenerateInviteCode(String userEmail, Long groupId) {
        User requester = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        GroupMember requesterMembership = requireMembership(groupId, requester.getId());
        if (requesterMembership.getRole() != GroupMemberRole.OWNER) {
            throw new ForbiddenException("Only the group owner can regenerate the invite link");
        }

        group.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        groupRepository.save(group);

        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    @Transactional
    public GroupResponse removeMember(String userEmail, Long groupId, Long memberUserId) {
        User requester = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        GroupMember requesterMembership = requireMembership(groupId, requester.getId());
        requireOpen(group);

        if (requesterMembership.getRole() != GroupMemberRole.OWNER) {
            throw new ForbiddenException("Only the group owner can remove members");
        }

        GroupMember target = memberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("That user is not a member of this group"));

        if (target.getRole() == GroupMemberRole.OWNER) {
            throw new ForbiddenException("The group owner cannot be removed. Delete the group instead.");
        }

        // Refuse to remove a member who still has an open balance - doing so
        // would silently erase who owes whom without a paper trail.
        BigDecimal net = balanceService.computeBalances(groupId).stream()
                .filter(b -> b.userId().equals(memberUserId))
                .findFirst()
                .map(b -> b.netBalance())
                .orElse(BigDecimal.ZERO);
        if (net.abs().compareTo(new BigDecimal("0.01")) >= 0) {
            throw new ForbiddenException("This member has an outstanding balance - settle up before removing them");
        }

        memberRepository.delete(target);
        activityService.log(group, requester, GroupActivityType.MEMBER_REMOVED,
                requester.getFullName() + " removed " + target.getUser().getFullName() + " from the group");
        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    @Transactional
    public void deleteGroup(String userEmail, Long groupId) {
        User requester = getUser(userEmail);
        GroupMember requesterMembership = requireMembership(groupId, requester.getId());
        if (requesterMembership.getRole() != GroupMemberRole.OWNER) {
            throw new ForbiddenException("Only the group owner can delete this group");
        }
        if (!groupExpenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId).isEmpty()) {
            throw new ForbiddenException("Cannot delete a group that still has expenses recorded");
        }
        groupRepository.deleteById(groupId);
    }

    /**
     * Closes a group: it becomes read-only. Members can still view all
     * expenses, balances and settlement history, but no new expenses,
     * members or settlements can be added, and existing membership can't
     * be changed until the group is reopened. Only the OWNER can do this.
     */
    @Transactional
    public GroupResponse closeGroup(String userEmail, Long groupId) {
        User requester = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        GroupMember requesterMembership = requireMembership(groupId, requester.getId());
        if (requesterMembership.getRole() != GroupMemberRole.OWNER) {
            throw new ForbiddenException("Only the group owner can close this group");
        }
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new ForbiddenException("This group is already closed");
        }

        group.setStatus(GroupStatus.CLOSED);
        group.setClosedAt(Instant.now());
        groupRepository.save(group);

        activityService.log(group, requester, GroupActivityType.GROUP_CLOSED, requester.getFullName() + " closed the group");

        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    /** Reopens a previously closed group, restoring normal read/write access. */
    @Transactional
    public GroupResponse reopenGroup(String userEmail, Long groupId) {
        User requester = getUser(userEmail);
        ExpenseGroup group = getGroupEntity(groupId);
        GroupMember requesterMembership = requireMembership(groupId, requester.getId());
        if (requesterMembership.getRole() != GroupMemberRole.OWNER) {
            throw new ForbiddenException("Only the group owner can reopen this group");
        }

        group.setStatus(GroupStatus.OPEN);
        group.setClosedAt(null);
        groupRepository.save(group);

        activityService.log(group, requester, GroupActivityType.GROUP_REOPENED, requester.getFullName() + " reopened the group");

        return mapper.toResponse(group, memberRepository.findByGroupId(groupId));
    }

    /** Guards any mutation (add/remove member, add expense, settle up) against a closed group. */
    void requireOpen(ExpenseGroup group) {
        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new ForbiddenException("This group is closed - reopen it to make changes");
        }
    }

    GroupMember requireMembership(Long groupId, Long userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group"));
    }

    ExpenseGroup getGroupEntity(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
