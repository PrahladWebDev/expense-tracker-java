package com.expense.tracker.group.repository;

import com.expense.tracker.group.entity.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {

    @Query("select distinct g from ExpenseGroup g join g.members m where m.user.id = :userId order by g.createdAt desc")
    List<ExpenseGroup> findAllForUser(@Param("userId") Long userId);

    java.util.Optional<ExpenseGroup> findByInviteCode(String inviteCode);
}
