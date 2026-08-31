package com.expense.tracker.auth.scheduled;

import com.expense.tracker.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * CONCEPT: Scheduled tasks
 * @Scheduled marks a method to be run automatically by Spring on a timer,
 * instead of in response to an HTTP request. Requires @EnableScheduling on
 * the main application class (ExpenseTrackerApplication) to activate the
 * scheduler infrastructure at all.
 *
 * WHY THIS JOB EXISTS: every login/refresh/rotation creates a new
 * refresh_tokens row; every rotation/logout marks the old one revoked
 * rather than deleting it (so we keep an audit trail briefly). Without
 * cleanup, this table grows forever. Running once a day is enough - these
 * rows have no urgency once they're unusable, we just don't want them
 * to accumulate indefinitely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000L) // once every 24 hours
    @Transactional
    public void purgeExpiredAndRevokedTokens() {
        int deleted = refreshTokenRepository.deleteExpiredOrRevoked(Instant.now());
        if (deleted > 0) {
            log.info("Refresh token cleanup: removed {} expired/revoked token(s)", deleted);
        }
    }
}
