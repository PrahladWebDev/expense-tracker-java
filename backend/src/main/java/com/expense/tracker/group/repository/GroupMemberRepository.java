package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupMember;
import com.expense.tracker.group.entity.GroupMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    long countByGroupIdAndRole(Long groupId, GroupMemberRole role);
}
