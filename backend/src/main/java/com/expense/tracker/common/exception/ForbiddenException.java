package com.expense.tracker.common.exception;

/** Thrown when an authenticated user tries to access a resource they don't own. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
