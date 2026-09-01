package com.expense.tracker.group.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A record of real-world money changing hands to settle a debt within a
 * group: `fromUser` paid `toUser` `amount` outside the app (cash, UPI,
 * bank transfer...). Recording it here adjusts the group's computed
 * balances so the debt isn't suggested again. This is a ledger entry, not
 * a payment gateway integration - nothing actually moves money.
 */
@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settlement_group", columnList = "group_id, settledAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant settledAt;

    @PrePersist
    protected void onCreate() {
        this.settledAt = Instant.now();
    }
}
