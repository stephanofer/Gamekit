package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.network.location.MatchLocation;
import network.hera.gamekit.network.location.MatchLocationRegistry;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisMatchLocationRegistry implements MatchLocationRegistry {

    private final RedisClient redis;
    private final Duration ttl;

    public CraftKitRedisMatchLocationRegistry(@NotNull RedisClient redis, @NotNull Duration ttl) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.ttl = Objects.requireNonNull(ttl, "ttl");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Match location TTL must be positive.");
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> save(@NotNull MatchLocation location) {
        return this.redis.cache()
            .set(key(location.matchId()), NetworkRedisPayloads.matchLocation(location), this.ttl)
            .thenApply(ignored -> null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<MatchLocation>> find(@NotNull MatchId matchId) {
        return this.redis.cache().get(key(matchId)).thenApply(payload -> payload == null
            ? Optional.empty()
            : Optional.of(NetworkRedisPayloads.matchLocation(payload)));
    }

    @Override
    public @NotNull CompletableFuture<Void> remove(@NotNull MatchId matchId) {
        return this.redis.cache().delete(key(matchId)).thenApply(ignored -> null);
    }

    private String key(final MatchId matchId) {
        return this.redis.key("gamekit", "match-location", matchId.toString());
    }
}
