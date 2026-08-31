package com.expense.tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * CONCEPT: JWT (JSON Web Token)
 * A JWT is a compact, self-contained, digitally SIGNED string that encodes
 * claims (e.g. "subject: user@example.com", "expires: <timestamp>").
 * Format: header.payload.signature (Base64Url-encoded, dot-separated).
 *
 * WHY JWT instead of server-side sessions?
 * A traditional session requires the server to store session state in
 * memory or a DB and look it up on every request. A JWT is STATELESS - the
 * server can verify it's authentic (via the signature) without a database
 * round-trip, which scales better across multiple server instances.
 * Trade-off: a JWT can't be easily "revoked" before it expires (this is why
 * we keep access tokens short-lived and use a separate, DB-backed refresh
 * token that CAN be revoked - see RefreshToken entity).
 *
 * ACCESS TOKEN vs REFRESH TOKEN:
 * - Access token: short-lived (15 min here), sent on every API request in
 *   the Authorization header, never stored server-side.
 * - Refresh token: long-lived (7 days here), stored in the database, used
 *   ONLY to obtain a new access token when the old one expires. This limits
 *   the damage window if an access token leaks, while keeping the user
 *   logged in for a reasonable time.
 *
 * CONCEPT: Dependency Injection via @Value
 * @Value("${app.jwt.secret}") pulls the value straight from application.yml
 * (which itself reads from an environment variable) - the secret never
 * needs to be hardcoded in source code.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
