package com.expense.tracker.group.entity;

/** The kinds of events shown in a group's activity feed (GroupActivity). */
public enum GroupActivityType {
    GROUP_CREATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    EXPENSE_ADDED,
    EXPENSE_DELETED,
    SETTLEMENT_RECORDED,
    COMMENT_ADDED,
    GROUP_CLOSED,
    GROUP_REOPENED
}
