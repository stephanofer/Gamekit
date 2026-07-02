package network.hera.gamekit.infra.craftkit.redis;

import com.hera.craftkit.redis.RedisClient;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.arena.registry.ArenaRegistry;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.time.GameKitClock;
import org.jetbrains.annotations.NotNull;

public final class CraftKitRedisArenaRegistry implements ArenaRegistry {

    private final RedisClient redis;
    private final GameKitClock clock;
    private final Duration indexTtl;
    private final Duration reservationLeaseTtl;

    public CraftKitRedisArenaRegistry(
            @NotNull RedisClient redis,
            @NotNull GameKitClock clock,
            @NotNull Duration indexTtl,
            @NotNull Duration reservationLeaseTtl
    ) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.indexTtl = requirePositive("indexTtl", indexTtl);
        this.reservationLeaseTtl = requirePositive("reservationLeaseTtl", reservationLeaseTtl);
    }

    @Override
    public @NotNull CompletableFuture<Void> recordSlot(@NotNull ArenaSlot slot) {
        Objects.requireNonNull(slot, "slot");
        return this.redis.cache()
            .set(arenaKey(slot.arenaId()), ArenaRedisPayloads.slot(slot), slot.ttl())
            .thenCompose(ignored -> this.redis.set().add(indexKey(slot.gameId()), slot.arenaId().toString()))
            .thenCompose(ignored -> this.redis.set().expire(indexKey(slot.gameId()), slot.ttl().plus(this.indexTtl)))
            .thenApply(ignored -> null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ArenaSlot>> find(@NotNull ArenaId arenaId) {
        Objects.requireNonNull(arenaId, "arenaId");
        return findStored(arenaId).thenApply(slot -> slot.filter(value -> !value.expiredAt(this.clock.now())));
    }

    @Override
    public @NotNull CompletableFuture<List<ArenaSlot>> findAvailable(@NotNull ArenaRequirements requirements) {
        Objects.requireNonNull(requirements, "requirements");
        return this.redis.set().members(indexKey(requirements.gameId())).thenCompose(arenaIds -> {
            if (arenaIds.isEmpty()) {
                return CompletableFuture.completedFuture(List.of());
            }
            final List<String> keys = arenaIds.stream()
                .map(id -> arenaKey(ArenaId.of(id)))
                .toList();
            return this.redis.cache().getMany(keys).thenApply(values -> values.values().stream()
                .map(ArenaRedisPayloads::slot)
                .filter(slot -> slot.matches(requirements, this.clock.now()))
                .toList());
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> tryReserve(@NotNull ArenaReservation reservation, @NotNull Duration reservationTtl) {
        Objects.requireNonNull(reservation, "reservation");
        requirePositive("reservationTtl", reservationTtl);
        if (!reservation.expiresAt().isAfter(this.clock.now())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Arena reservation is already expired."));
        }
        return this.redis.coordinator().withLease(lockKey(reservation.arenaId()), this.reservationLeaseTtl, () ->
            findStored(reservation.arenaId()).thenCompose(slot -> {
                if (slot.isEmpty() || !slot.get().availableAt(this.clock.now())) {
                    return CompletableFuture.completedFuture(false);
                }
                if (!slot.get().serverId().equals(reservation.serverId()) || !slot.get().gameId().equals(reservation.gameId())) {
                    return CompletableFuture.completedFuture(false);
                }
                return writeReservation(slot.get(), reservation, reservationTtl);
            })
        ).thenApply(result -> result.orElse(false));
    }

    @Override
    public @NotNull CompletableFuture<Void> updateState(@NotNull ArenaId arenaId, @NotNull ArenaState state) {
        Objects.requireNonNull(state, "state");
        return findStored(arenaId).thenCompose(slot -> {
            if (slot.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            return recordSlot(slot.get().withState(state, this.clock.now()));
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> markDisabled(@NotNull ArenaId arenaId) {
        return updateState(arenaId, ArenaState.DISABLED);
    }

    private CompletableFuture<Boolean> writeReservation(final ArenaSlot original, final ArenaReservation reservation, final Duration reservationTtl) {
        final ArenaSlot reserved = original.withState(ArenaState.RESERVED, this.clock.now());
        return this.redis.cache()
            .set(arenaKey(reserved.arenaId()), ArenaRedisPayloads.slot(reserved), reserved.ttl())
            .thenCompose(ignored -> this.redis.cache().set(reservationKey(reservation.arenaId()), ArenaRedisPayloads.reservation(reservation), reservationTtl))
            .thenApply(ignored -> true)
            .exceptionallyCompose(failure -> rollbackReservation(original, reservation, failure));
    }

    private CompletableFuture<Boolean> rollbackReservation(final ArenaSlot original, final ArenaReservation reservation, final Throwable failure) {
        return this.redis.cache()
            .set(arenaKey(original.arenaId()), ArenaRedisPayloads.slot(original), original.ttl())
            .thenCompose(ignored -> this.redis.cache().delete(reservationKey(reservation.arenaId())))
            .handle((ignored, rollbackFailure) -> {
                if (rollbackFailure != null) {
                    failure.addSuppressed(rollbackFailure);
                }
                return failure;
            })
            .thenCompose(error -> CompletableFuture.failedFuture(error));
    }

    private CompletableFuture<Optional<ArenaSlot>> findStored(final ArenaId arenaId) {
        return this.redis.cache().get(arenaKey(arenaId)).thenApply(payload -> payload == null
            ? Optional.empty()
            : Optional.of(ArenaRedisPayloads.slot(payload)));
    }

    private String arenaKey(final ArenaId arenaId) {
        return this.redis.key("gamekit", "arena", arenaId.toString());
    }

    private String indexKey(final GameId gameId) {
        return this.redis.key("gamekit", "arena-index", gameId.toString());
    }

    private String reservationKey(final ArenaId arenaId) {
        return this.redis.key("gamekit", "arena-reservation", arenaId.toString());
    }

    private String lockKey(final ArenaId arenaId) {
        return this.redis.key("gamekit", "arena-lock", arenaId.toString());
    }

    private static Duration requirePositive(final String field, final Duration duration) {
        Objects.requireNonNull(duration, field);
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(field + " must be positive.");
        }
        return duration;
    }
}
