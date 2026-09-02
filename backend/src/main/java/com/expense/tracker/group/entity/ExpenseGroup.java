package com.expense.tracker.group.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT: Why "ExpenseGroup" and not "Group"
 * GROUP is a reserved SQL keyword (used in GROUP BY). Naming the entity
 * "Group" would force every generated query to quote the table name.
 * Calling it ExpenseGroup (table "expense_groups") sidesteps that entirely
 * and reads clearly next to the existing Expense entity.
 *
 * A group is a shared "pot" (e.g. "Goa Trip", "Flatmates") that a set of
 * users belong to. Group expenses are paid by one member on behalf of the
 * group and split across some subset of members; balances/settlements are
 * always scoped to a single group.
 */
@Entity
@Table(name = "expense_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * OPEN groups accept new expenses/members/settlements. CLOSED groups
     * are read-only - see GroupStatus for details. Defaults to OPEN.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupStatus status = GroupStatus.OPEN;

    /** When the group was closed; null while the group is OPEN. */
    private Instant closedAt;

    /**
     * A short random code that lets anyone with the link join the group
     * without needing to already be a registered contact - see
     * GroupService#joinByInviteCode. Regenerating it (owner-only)
     * invalidates any link that was shared before.
     */
    @Column(unique = true, length = 12)
    private String inviteCode;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = GroupStatus.OPEN;
        }
        if (this.inviteCode == null) {
            this.inviteCode = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        }
    }
}
