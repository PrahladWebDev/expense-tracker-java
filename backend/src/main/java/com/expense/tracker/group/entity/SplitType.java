package com.expense.tracker.group.entity;

/**
 * How a GroupExpense's amount is divided among the selected participants.
 *   EQUAL      - amount is split evenly across every participant listed.
 *   EXACT      - caller supplies the exact rupee amount each participant owes;
 *                the amounts must sum to the total expense amount.
 *   PERCENTAGE - caller supplies each participant's percentage share; the
 *                percentages must sum to 100.
 */
public enum SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE
}
