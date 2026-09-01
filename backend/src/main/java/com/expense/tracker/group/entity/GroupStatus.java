package com.expense.tracker.group.entity;

/**
 * OPEN groups accept new expenses, members and settlements as normal.
 * CLOSED groups are read-only: their history (expenses, balances,
 * settlements) can still be viewed, but nothing new can be added and
 * membership can no longer be changed. Only the group OWNER can close
 * or reopen a group.
 */
public enum GroupStatus {
    OPEN,
    CLOSED
}
