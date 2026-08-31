package com.expense.tracker.common.exception;

/** Thrown when trying to create a resource that violates a uniqueness rule (e.g. email already registered). */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
