package network.hera.gamekit.velocity.config;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public record VelocityRuntimeConfig(
        @NotNull RedisSettings redis,
        @NotNull RoutingSettings routing,
        @NotNull FallbackSettings fallback
) {

    public VelocityRuntimeConfig {
        Objects.requireNonNull(redis, "redis");
        Objects.requireNonNull(routing, "routing");
        Objects.requireNonNull(fallback, "fallback");
    }

    public record RedisSettings(
            @NotNull String host,
            int port,
            int database,
            @NotNull String username,
            @NotNull String password,
            boolean ssl,
            boolean verifyPeer,
            @NotNull String keyPrefix,
            @NotNull String environment,
            @NotNull String serverId
    ) {
        public RedisSettings {
            Objects.requireNonNull(host, "host");
            Objects.requireNonNull(username, "username");
            Objects.requireNonNull(password, "password");
            Objects.requireNonNull(keyPrefix, "keyPrefix");
            Objects.requireNonNull(environment, "environment");
            Objects.requireNonNull(serverId, "serverId");
        }
    }

    public record RoutingSettings(
            boolean strictGameKitServerValidation,
            @NotNull List<ServerId> protectedServerIds,
            @NotNull Duration transferTimeout,
            @NotNull Duration inflightTtl
    ) {
        public RoutingSettings {
            protectedServerIds = List.copyOf(Objects.requireNonNull(protectedServerIds, "protectedServerIds"));
            Objects.requireNonNull(transferTimeout, "transferTimeout");
            Objects.requireNonNull(inflightTtl, "inflightTtl");
            if (transferTimeout.isZero() || transferTimeout.isNegative()) {
                throw new IllegalArgumentException("Velocity transfer timeout must be positive.");
            }
            if (inflightTtl.isZero() || inflightTtl.isNegative()) {
                throw new IllegalArgumentException("Velocity inflight TTL must be positive.");
            }
        }
    }

    public record FallbackSettings(
            ServerId defaultLobbyServerId,
            boolean disconnectIfNoFallback
    ) {
    }
}
