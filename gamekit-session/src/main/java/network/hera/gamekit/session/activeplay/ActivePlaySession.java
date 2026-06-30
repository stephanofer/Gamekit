package network.hera.gamekit.session.activeplay;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import org.jetbrains.annotations.NotNull;

public record ActivePlaySession(
        @NotNull PlayerId playerId,
        @NotNull GameId gameId,
        @NotNull ActivePlayType type,
        @NotNull String referenceId,
        @NotNull Instant createdAt,
        @NotNull Optional<Instant> expiresAt
) {

    public ActivePlaySession {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException("referenceId must not be blank");
        }
        referenceId = referenceId.trim();
    }

    public static @NotNull ActivePlaySession queue(
            @NotNull PlayerId playerId,
            @NotNull QueueId queueId,
            @NotNull Instant createdAt
    ) {
        return new ActivePlaySession(playerId, queueId.gameId(), ActivePlayType.QUEUE, queueId.toString(), createdAt, Optional.empty());
    }

    public static @NotNull ActivePlaySession waitingRoom(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull String waitingRoomId,
            @NotNull Instant createdAt
    ) {
        return new ActivePlaySession(playerId, gameId, ActivePlayType.WAITING_ROOM, waitingRoomId, createdAt, Optional.empty());
    }

    public static @NotNull ActivePlaySession matchStarting(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull String matchId,
            @NotNull Instant createdAt
    ) {
        return new ActivePlaySession(playerId, gameId, ActivePlayType.MATCH_STARTING, matchId, createdAt, Optional.empty());
    }

    public static @NotNull ActivePlaySession matchRunning(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull String matchId,
            @NotNull Instant createdAt
    ) {
        return new ActivePlaySession(playerId, gameId, ActivePlayType.MATCH_RUNNING, matchId, createdAt, Optional.empty());
    }

    public static @NotNull ActivePlaySession spectating(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull String matchId,
            @NotNull Instant createdAt
    ) {
        return new ActivePlaySession(playerId, gameId, ActivePlayType.SPECTATING, matchId, createdAt, Optional.empty());
    }

    public static @NotNull ActivePlaySession reconnectableMatch(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull String matchId,
            @NotNull Instant createdAt,
            @NotNull Instant expiresAt
    ) {
        return new ActivePlaySession(playerId, gameId, ActivePlayType.RECONNECTABLE_MATCH, matchId, createdAt, Optional.of(expiresAt));
    }
}
