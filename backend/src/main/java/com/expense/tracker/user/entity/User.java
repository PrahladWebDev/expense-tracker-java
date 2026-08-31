package com.expense.tracker.user.entity;

import com.expense.tracker.budget.entity.Budget;
import com.expense.tracker.expense.entity.Expense;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT: JPA Entity
 * @Entity tells Hibernate (the JPA implementation) that this class maps to
 * a database table. Each field maps to a column; each instance maps to a row.
 * This is the ORM (Object-Relational Mapping) pattern: it lets us work with
 * plain Java objects instead of writing raw SQL for every operation.
 *
 * CONCEPT: Lombok annotations
 * @Getter/@Setter generate getX()/setX() methods at compile time - no manual
 * boilerplate. @NoArgsConstructor generates an empty constructor (JPA
 * REQUIRES one so Hibernate can instantiate entities via reflection).
 * @AllArgsConstructor generates a constructor with every field.
 * @Builder gives us a fluent `User.builder().email(...).build()` API,
 * which is more readable than a long constructor call, especially with
 * many optional fields.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash, never plain text

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * CONCEPT: One-to-many relationship
     * One User has MANY Expenses. `mappedBy = "user"` means the Expense
     * entity owns the foreign key column (expenses.user_id) - User does not
     * have a user_id column of its own for this relationship.
     * We generally don't fetch this list eagerly (see Expense's fetch type)
     * to avoid loading a user's entire expense history just to log them in.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Budget> budgets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
    }
}
