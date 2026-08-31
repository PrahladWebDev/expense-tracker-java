package com.expense.tracker.auth.repository;

import com.expense.tracker.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);

    /**
     * @Modifying tells Spring Data this @Query performs a write (DELETE),
     * not a read - without it, Spring Data would try to interpret the
     * query as something returning entities and fail at startup.
     * Used by RefreshTokenCleanupJob to periodically purge rows that can
     * never be used again (expired, or already revoked by rotation/logout),
     * so this table doesn't grow forever in a long-running deployment.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    int deleteExpiredOrRevoked(Instant now);
}
