package network.hera.gamekit.testkit.network;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerHeartbeat;
import network.hera.gamekit.network.registry.ServerRegistry;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;
import org.jetbrains.annotations.NotNull;

public final class FakeServerRegistry implements ServerRegistry {

    private final GameKitClock clock;
    private final Map<ServerId, RegisteredServer> servers = new ConcurrentHashMap<>();

    public FakeServerRegistry(@NotNull GameKitClock clock) {
        this.clock = clock;
    }

    @Override
    public @NotNull CompletableFuture<Void> recordHeartbeat(@NotNull ServerHeartbeat heartbeat) {
        this.servers.put(heartbeat.serverId(), heartbeat.toRegisteredServer());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<RegisteredServer>> find(@NotNull ServerId serverId) {
        final RegisteredServer server = this.servers.get(serverId);
        return CompletableFuture.completedFuture(Optional.ofNullable(server));
    }

    @Override
    public @NotNull CompletableFuture<List<RegisteredServer>> findAvailable(@NotNull GameId gameId, @NotNull ServerRole role) {
        final List<RegisteredServer> available = this.servers.values().stream()
            .filter(server -> server.gameId().equals(gameId))
            .filter(server -> server.role() == role)
            .filter(server -> server.acceptsNewWorkAt(this.clock.now()))
            .sorted(Comparator.comparing(server -> server.serverId().toString()))
            .toList();
        return CompletableFuture.completedFuture(available);
    }

    @Override
    public @NotNull CompletableFuture<Void> markOffline(@NotNull ServerId serverId) {
        final RegisteredServer server = this.servers.get(serverId);
        if (server != null) {
            this.servers.put(serverId, new RegisteredServer(
                server.serverId(),
                server.gameId(),
                server.role(),
                ServerState.OFFLINE,
                server.capacity(),
                server.onlinePlayers(),
                server.lastHeartbeatAt(),
                server.heartbeatTtl()
            ));
        }
        return CompletableFuture.completedFuture(null);
    }
}
