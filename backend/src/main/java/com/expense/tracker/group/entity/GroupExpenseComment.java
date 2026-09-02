package com.expense.tracker.group.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** A free-text note left by a group member on a specific GroupExpense, e.g. "paid the auto fare too". */
@Entity
@Table(name = "group_expense_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpenseComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_expense_id", nullable = false)
    private GroupExpense groupExpense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
