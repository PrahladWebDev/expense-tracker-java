package com.expense.tracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CONCEPT: DTO (Data Transfer Object)
 * A DTO is a plain object used ONLY to carry data between layers (e.g.
 * React -> Controller). We NEVER expose our JPA @Entity classes directly
 * in the API:
 *   1. Security: entities can carry sensitive fields (password hash) that
 *      must never be serialized into a JSON response.
 *   2. Decoupling: the database schema can evolve independently of the
 *      public API contract.
 *   3. Validation: request DTOs are the natural place to attach Bean
 *      Validation annotations (@NotBlank, @Email...) for INCOMING data.
 *
 * CONCEPT: Bean Validation annotations
 * These annotations are processed by Hibernate Validator when a controller
 * parameter is marked @Valid. If validation fails, a
 * MethodArgumentNotValidException is thrown and caught by our
 * GlobalExceptionHandler, which turns it into a 400 response.
 */
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be under 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password
) {}
