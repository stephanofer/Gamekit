package network.hera.gamekit.testkit.arena;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.arena.registry.ArenaRegistry;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.time.GameKitClock;
import org.jetbrains.annotations.NotNull;

public final class FakeArenaRegistry implements ArenaRegistry {

    private final GameKitClock clock;
    private final Map<ArenaId, ArenaSlot> slots = new ConcurrentHashMap<>();
    private final Map<ArenaId, ArenaReservation> reservations = new ConcurrentHashMap<>();

    public FakeArenaRegistry(@NotNull GameKitClock clock) {
        this.clock = clock;
    }

    @Override
    public @NotNull CompletableFuture<Void> recordSlot(@NotNull ArenaSlot slot) {
        this.slots.put(slot.arenaId(), slot);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ArenaSlot>> find(@NotNull ArenaId arenaId) {
        final ArenaSlot slot = this.slots.get(arenaId);
        if (slot == null || slot.expiredAt(this.clock.now())) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.completedFuture(Optional.of(slot));
    }

    @Override
    public @NotNull CompletableFuture<List<ArenaSlot>> findAvailable(@NotNull ArenaRequirements requirements) {
        final List<ArenaSlot> available = this.slots.values().stream()
            .filter(slot -> slot.matches(requirements, this.clock.now()))
            .sorted(Comparator.comparing(slot -> slot.arenaId().toString()))
            .toList();
        return CompletableFuture.completedFuture(available);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> tryReserve(@NotNull ArenaReservation reservation, @NotNull Duration reservationTtl) {
        if (reservationTtl.isZero() || reservationTtl.isNegative()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Arena reservation TTL must be positive."));
        }
        synchronized (this.slots) {
            final ArenaSlot slot = this.slots.get(reservation.arenaId());
            if (slot == null || !slot.availableAt(this.clock.now())) {
                return CompletableFuture.completedFuture(false);
            }
            if (!slot.serverId().equals(reservation.serverId()) || !slot.gameId().equals(reservation.gameId())) {
                return CompletableFuture.completedFuture(false);
            }
            this.slots.put(slot.arenaId(), slot.withState(ArenaState.RESERVED, this.clock.now()));
            this.reservations.put(slot.arenaId(), reservation);
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> updateState(@NotNull ArenaId arenaId, @NotNull ArenaState state) {
        this.slots.computeIfPresent(arenaId, (ignored, slot) -> slot.withState(state, this.clock.now()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> markDisabled(@NotNull ArenaId arenaId) {
        return updateState(arenaId, ArenaState.DISABLED);
    }

    public boolean hasReservation(@NotNull ArenaId arenaId) {
        return this.reservations.containsKey(arenaId);
    }
}
