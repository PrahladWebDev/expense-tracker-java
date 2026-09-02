package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupActivityRepository extends JpaRepository<GroupActivity, Long> {
    List<GroupActivity> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
}
