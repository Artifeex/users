package ru.sandr.users.security.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sandr.users.security.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Deletes all refresh tokens for the user except the newest {@code keepN} tokens (by {@code created_at} DESC).
     * Uses a single native SQL statement (fast, race-safe inside a transaction).
     */
    @Modifying
    @Query(value = """
            DELETE FROM auth.refresh_token
            WHERE user_id = :userId
              AND id IN (
                SELECT id
                FROM auth.refresh_token
                WHERE user_id = :userId
                ORDER BY created_at DESC
                OFFSET :keepN
              )
            """, nativeQuery = true)
    void deleteTokensOlderThanTopN(@Param("userId") UUID userId, @Param("keepN") int keepN);
}
