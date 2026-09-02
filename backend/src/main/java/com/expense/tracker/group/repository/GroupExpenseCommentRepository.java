package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.GroupExpenseComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupExpenseCommentRepository extends JpaRepository<GroupExpenseComment, Long> {
    List<GroupExpenseComment> findByGroupExpenseIdOrderByCreatedAtAsc(Long groupExpenseId);
    Optional<GroupExpenseComment> findByIdAndGroupExpenseId(Long id, Long groupExpenseId);
}
