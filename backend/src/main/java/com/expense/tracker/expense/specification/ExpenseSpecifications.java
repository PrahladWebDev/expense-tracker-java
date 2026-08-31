package com.expense.tracker.expense.specification;

import com.expense.tracker.expense.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CONCEPT: JPA Specification (Criteria API wrapper)
 * A Specification<Expense> is essentially a small piece of a WHERE clause,
 * expressed as a lambda: (root, query, criteriaBuilder) -> Predicate.
 * Spring Data JPA lets us COMBINE Specifications with `.and()` - only
 * combining the ones that are actually needed for a given request.
 *
 * WHY this approach over derived query methods or raw @Query strings here:
 * Search/filter/sort on expenses is fully OPTIONAL and COMBINABLE (any
 * subset of: search text + category + date range + amount range). A single
 * @Query can't conditionally include/exclude clauses; Specifications can,
 * because we only .and() the ones whose parameter isn't null.
 *
 * CONCEPT: Static factory methods + null-safety
 * Each method returns `null` when its filter isn't provided; callers filter
 * out nulls before combining, so unset filters contribute NO restriction.
 */
public class ExpenseSpecifications {

    public static Specification<Expense> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Expense> descriptionContains(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), pattern);
    }

    public static Specification<Expense> hasCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> dateFrom(LocalDate from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), from);
    }

    public static Specification<Expense> dateTo(LocalDate to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), to);
    }

    public static Specification<Expense> amountMin(BigDecimal min) {
        if (min == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Expense> amountMax(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }
}
