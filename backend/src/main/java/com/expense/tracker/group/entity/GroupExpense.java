package com.expense.tracker.group.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A single group expense: one member (paidBy) covered the whole `amount`,
 * and it is divided across the members listed in `shares`. This is
 * deliberately separate from the personal Expense entity - group expenses
 * are shared, multi-party, and drive balances/settlements, whereas Expense
 * is a private single-user record.
 */
@Entity
@Table(name = "group_expenses", indexes = {
        @Index(name = "idx_group_expense_group_date", columnList = "group_id, expenseDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;

    @OneToMany(mappedBy = "groupExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupExpenseShare> shares = new ArrayList<>();

    /** Stored (random UUID) filename of an uploaded receipt photo, if any - see FileStorageService. */
    private String receiptStoredName;

    /** Original filename the receipt was uploaded with, shown in the UI instead of the random stored name. */
    private String receiptOriginalName;

    private String receiptContentType;

    /**
     * Soft delete: a deleted group expense is never physically removed.
     * It stays visible in the group's expense list (struck through, with
     * who deleted it) for a clear audit trail, but is excluded from every
     * balance/total calculation as if it never happened - see
     * GroupExpenseRepository.findByGroupIdAndDeletedFalse... and the
     * "AND s.groupExpense.deleted = false" clauses in
     * GroupExpenseShareRepository.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
