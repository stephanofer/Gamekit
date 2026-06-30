package network.hera.gamekit.network.registry;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public record RegisteredServer(
        @NotNull ServerId serverId,
        @NotNull GameId gameId,
        @NotNull ServerRole role,
        @NotNull ServerState state,
        int capacity,
        int onlinePlayers,
        @NotNull Instant lastHeartbeatAt,
        @NotNull Duration heartbeatTtl
) {

    public RegisteredServer {
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(lastHeartbeatAt, "lastHeartbeatAt");
        Objects.requireNonNull(heartbeatTtl, "heartbeatTtl");
        if (capacity < 0) {
            throw new IllegalArgumentException("Server capacity must not be negative.");
        }
        if (onlinePlayers < 0) {
            throw new IllegalArgumentException("Server online players must not be negative.");
        }
        if (onlinePlayers > capacity) {
            throw new IllegalArgumentException("Server online players must not exceed capacity.");
        }
        if (heartbeatTtl.isZero() || heartbeatTtl.isNegative()) {
            throw new IllegalArgumentException("Server heartbeat TTL must be positive.");
        }
    }

    public boolean heartbeatExpiredAt(@NotNull Instant now) {
        return !this.lastHeartbeatAt.plus(this.heartbeatTtl).isAfter(Objects.requireNonNull(now, "now"));
    }

    public @NotNull ServerState stateAt(@NotNull Instant now) {
        if (this.state == ServerState.OFFLINE) {
            return ServerState.OFFLINE;
        }
        if (heartbeatExpiredAt(now)) {
            return ServerState.UNKNOWN;
        }
        return this.state;
    }

    public boolean acceptsNewWorkAt(@NotNull Instant now) {
        return stateAt(now).acceptsNewWork() && this.onlinePlayers < this.capacity;
    }

    public boolean acceptsExistingMatchRoutingAt(@NotNull Instant now) {
        return stateAt(now).acceptsExistingMatchRouting();
    }
}
