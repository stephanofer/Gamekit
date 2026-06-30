package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerHeartbeat;
import network.hera.gamekit.network.registry.ServerRegistry;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisServerRegistry implements ServerRegistry {

    private final RedisClient redis;
    private final GameKitClock clock;
    private final Duration indexTtl;

    public CraftKitRedisServerRegistry(@NotNull RedisClient redis, @NotNull GameKitClock clock, @NotNull Duration indexTtl) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.indexTtl = Objects.requireNonNull(indexTtl, "indexTtl");
        if (indexTtl.isZero() || indexTtl.isNegative()) {
            throw new IllegalArgumentException("Server registry index TTL must be positive.");
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> recordHeartbeat(@NotNull ServerHeartbeat heartbeat) {
        final RegisteredServer server = heartbeat.toRegisteredServer();
        return this.redis.cache()
            .set(serverKey(server.serverId()), NetworkRedisPayloads.server(server), server.heartbeatTtl().plus(this.indexTtl))
            .thenCompose(ignored -> addToIndex(server))
            .thenApply(ignored -> null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<RegisteredServer>> find(@NotNull ServerId serverId) {
        return this.redis.cache().get(serverKey(serverId)).thenApply(payload -> payload == null
            ? Optional.empty()
            : Optional.of(NetworkRedisPayloads.server(payload)));
    }

    @Override
    public @NotNull CompletableFuture<List<RegisteredServer>> findAvailable(@NotNull GameId gameId, @NotNull ServerRole role) {
        return this.redis.set().members(indexKey(gameId, role)).thenCompose(serverIds -> {
            if (serverIds.isEmpty()) {
                return CompletableFuture.completedFuture(List.of());
            }
            final List<String> keys = serverIds.stream().map(id -> serverKey(ServerId.of(id))).toList();
            return this.redis.cache().getMany(keys).thenApply(values -> values.values().stream()
                .map(NetworkRedisPayloads::server)
                .filter(server -> server.gameId().equals(gameId))
                .filter(server -> server.role() == role)
                .filter(server -> server.acceptsNewWorkAt(this.clock.now()))
                .toList());
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> markOffline(@NotNull ServerId serverId) {
        return find(serverId).thenCompose(server -> {
            if (server.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            final RegisteredServer offline = new RegisteredServer(
                server.get().serverId(),
                server.get().gameId(),
                server.get().role(),
                ServerState.OFFLINE,
                server.get().capacity(),
                server.get().onlinePlayers(),
                this.clock.now(),
                server.get().heartbeatTtl()
            );
            return this.redis.cache().set(serverKey(serverId), NetworkRedisPayloads.server(offline), server.get().heartbeatTtl()).thenApply(ignored -> null);
        });
    }

    private CompletableFuture<Boolean> addToIndex(final RegisteredServer server) {
        final String key = indexKey(server.gameId(), server.role());
        return this.redis.set()
            .add(key, server.serverId().toString())
            .thenCompose(ignored -> this.redis.set().expire(key, server.heartbeatTtl().plus(this.indexTtl)));
    }

    private String serverKey(final ServerId serverId) {
        return this.redis.key("gamekit", "server", serverId.toString());
    }

    private String indexKey(final GameId gameId, final ServerRole role) {
        return this.redis.key("gamekit", "server-index", gameId.toString(), role.name().toLowerCase());
    }

}
