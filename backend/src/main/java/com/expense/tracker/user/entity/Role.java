package com.expense.tracker.user.entity;

/**
 * CONCEPT: Enum (Enumeration)
 * An enum is a special Java type representing a fixed set of constants.
 * Instead of using raw strings like "USER" or "ADMIN" (typo-prone, no
 * compile-time safety), we define exactly the values that are valid.
 *
 * WHY: Role-based authorization needs a closed set of roles. The compiler
 * will catch typos (Role.ADMIN vs "admin"), and switch statements over an
 * enum can be checked for exhaustiveness.
 */
public enum Role {
    USER,
    ADMIN
}
