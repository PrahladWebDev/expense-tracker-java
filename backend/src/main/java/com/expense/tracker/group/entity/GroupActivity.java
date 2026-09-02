package com.expense.tracker.group.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A single "what happened" entry in a group's activity feed, e.g.
 * "Priya added ₹500 for dinner". This app has no push notifications /
 * websockets, so the feed is pull-based: the frontend polls
 * GET /groups/{id}/activity, the same way it already polls balances and
 * expenses after a mutation (see useGroups.ts's invalidateQueries calls).
 * `message` is pre-rendered at write time (not composed client-side) so the
 * feed still reads correctly even if the actor or a referenced expense is
 * later deleted.
 */
@Entity
@Table(name = "group_activities", indexes = {
        @Index(name = "idx_group_activity_group_created", columnList = "group_id, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GroupActivityType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
