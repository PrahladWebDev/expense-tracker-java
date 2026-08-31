package com.expense.tracker.expense.entity;

import com.expense.tracker.category.entity.Category;
import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * CONCEPT: BigDecimal for money
 * We use BigDecimal, NOT double/float, for the `amount` field.
 * WHY: floating-point types (double/float) represent numbers in binary,
 * and many decimal fractions (like 0.1) CANNOT be represented exactly in
 * binary - this causes tiny rounding errors that compound over many
 * calculations (e.g. 0.1 + 0.2 != 0.3 in floating point!). For money, even
 * a fraction-of-a-cent error is unacceptable. BigDecimal stores numbers as
 * an exact, arbitrary-precision decimal, so 19.99 + 0.01 is EXACTLY 20.00.
 * We also pin the DB column to DECIMAL(12,2) so the database itself never
 * silently loses precision either.
 */
@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_user_date", columnList = "user_id, expenseDate"),
        @Index(name = "idx_expense_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate expenseDate;

    /**
     * CONCEPT: Many-to-one relationship
     * Many Expenses belong to one User, and many Expenses belong to one
     * Category. FetchType.LAZY means Hibernate does NOT load the related
     * User/Category from the DB until you actually call .getUser() /
     * .getCategory() - this avoids unnecessary joins/queries when we only
     * need the expense's own fields (e.g. listing expenses and only
     * displaying category.name via a DTO projection).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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
