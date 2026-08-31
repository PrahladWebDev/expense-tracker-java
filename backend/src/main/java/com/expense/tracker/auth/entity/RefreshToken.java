package com.expense.tracker.auth.entity;

import com.expense.tracker.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * WHY store refresh tokens in the database (unlike access tokens)?
 * Refresh tokens are long-lived, so we need the ability to REVOKE one
 * (e.g. on logout, or if a device is stolen) before it naturally expires.
 * A purely stateless JWT can't be revoked early - so refresh tokens are
 * stored server-side and checked against the DB on every /refresh call.
 * This is "refresh token rotation": each refresh can invalidate the old
 * token and issue a new one, limiting how long a stolen refresh token stays useful.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
