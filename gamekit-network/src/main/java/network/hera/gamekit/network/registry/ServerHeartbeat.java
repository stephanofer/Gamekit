package network.hera.gamekit.network.registry;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public record ServerHeartbeat(
        @NotNull ServerId serverId,
        @NotNull GameId gameId,
        @NotNull ServerRole role,
        @NotNull ServerState state,
        int capacity,
        int onlinePlayers,
        @NotNull Instant observedAt,
        @NotNull Duration ttl
) {

    public ServerHeartbeat {
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(observedAt, "observedAt");
        Objects.requireNonNull(ttl, "ttl");
    }

    public @NotNull RegisteredServer toRegisteredServer() {
        return new RegisteredServer(this.serverId, this.gameId, this.role, this.state, this.capacity, this.onlinePlayers, this.observedAt, this.ttl);
    }
}
