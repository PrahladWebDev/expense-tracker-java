package com.expense.tracker.common.exception;

/**
 * A custom, unchecked (RuntimeException) exception thrown whenever a
 * requested entity (Expense, Category, Budget, User...) can't be found.
 *
 * CONCEPT: Exception handling
 * Java distinguishes CHECKED exceptions (must be declared with `throws` or
 * caught - e.g. IOException) from UNCHECKED exceptions (RuntimeException and
 * its subclasses - don't need to be declared).
 * We extend RuntimeException here because "not found" is an expected,
 * recoverable business condition, not a catastrophic programming error -
 * we don't want every service method signature cluttered with `throws`.
 * It's caught centrally by GlobalExceptionHandler and turned into a 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
