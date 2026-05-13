package com.snack24.identity.auth.repository;

import com.snack24.identity.auth.RefreshTokenValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private static final String KEY_PREFIX = "refresh:token:";
    private static final String MEMBER_TOKENS_KEY = "refresh:member:%d";
    private static final String REVOKED_PREFIX = "refresh:revoked:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String rawToken, Long memberId, Duration ttl) {
        String hash = sha256(rawToken);
        RefreshTokenValue refreshTokenValue = new RefreshTokenValue(memberId, Instant.now().plus(ttl));
        redisTemplate.opsForValue().set(KEY_PREFIX + hash, refreshTokenValue, ttl);
        redisTemplate.opsForSet().add(MEMBER_TOKENS_KEY.formatted(memberId), hash);
    }

    public Optional<RefreshTokenValue> find(String rawToken) {
        String hash = sha256(rawToken);
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + hash);
        return Optional.ofNullable((RefreshTokenValue) value);
    }

    public void rotate(String oldRawToken, String newRawToken, Long memberId, Duration ttl) {
        delete(oldRawToken, memberId);
        save(newRawToken, memberId, ttl);
    }

    public void revokeAll(Long memberId) {
        String memberKey = MEMBER_TOKENS_KEY.formatted(memberId);
        Set<Object> hashes = redisTemplate.opsForSet().members(memberKey);
        if (hashes != null) {
            for (Object hash : hashes) {
                redisTemplate.delete(KEY_PREFIX + hash);
            }
        }
        redisTemplate.delete(memberKey);
    }

    public void markedRevoked(String rawToken, Long memberId, Duration ttl) {
        redisTemplate.opsForValue().set(REVOKED_PREFIX + sha256(rawToken), memberId, ttl);
    }

    public Optional<Long> findRevokedMemberId(String rawToken) {
        Object value = redisTemplate.opsForValue().get(REVOKED_PREFIX + sha256(rawToken));
        if (value == null) return Optional.empty();
        return Optional.of(((Number) value).longValue());
    }

    public boolean isRevoked(String rawToken) {
        return redisTemplate.hasKey(REVOKED_PREFIX + sha256(rawToken));
    }

    public void delete(String rawToken, Long memberId) {
        String hash = sha256(rawToken);
        redisTemplate.delete(KEY_PREFIX + hash);
        redisTemplate.opsForSet().remove(MEMBER_TOKENS_KEY.formatted(memberId), hash);

    }

    private static String sha256(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
