package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionRequestId;
import network.hera.gamekit.network.admission.AdmissionStore;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisAdmissionStore implements AdmissionStore {

    private final RedisClient redis;
    private final GameKitClock clock;

    public CraftKitRedisAdmissionStore(@NotNull RedisClient redis, @NotNull GameKitClock clock) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public @NotNull CompletableFuture<Void> save(@NotNull AdmissionRequest request) {
        final Duration ttl = Duration.between(this.clock.now(), request.expiresAt());
        if (ttl.isZero() || ttl.isNegative()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Admission request is already expired."));
        }
        return this.redis.cache()
            .set(key(request.id()), NetworkRedisPayloads.admission(request), ttl)
            .thenApply(ignored -> null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<AdmissionRequest>> find(@NotNull AdmissionRequestId requestId) {
        return this.redis.cache().get(key(requestId)).thenApply(this::decodeActive);
    }

    @Override
    public @NotNull CompletableFuture<Optional<AdmissionRequest>> consume(@NotNull AdmissionRequestId requestId) {
        return this.redis.state().getAndDelete(key(requestId)).thenApply(this::decodeActive);
    }

    private Optional<AdmissionRequest> decodeActive(final String payload) {
        if (payload == null) {
            return Optional.empty();
        }
        final AdmissionRequest request = NetworkRedisPayloads.admission(payload);
        if (request.expiredAt(this.clock.now())) {
            return Optional.empty();
        }
        return Optional.of(request);
    }

    private String key(final AdmissionRequestId requestId) {
        return this.redis.key("gamekit", "admission", requestId.toString());
    }
}
