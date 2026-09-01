package com.expense.tracker.group.entity;

/**
 * OWNER created the group (or was promoted) and can add/remove members and
 * delete the group. MEMBER can add expenses, add other members, and settle
 * up, but cannot remove members or delete the group.
 */
public enum GroupMemberRole {
    OWNER,
    MEMBER
}
