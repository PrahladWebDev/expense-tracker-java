package com.expense.tracker.common.exception;

/** Thrown when credentials are invalid or a token is invalid/expired. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
