package network.hera.gamekit.arena.registry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.core.id.ArenaId;
import org.jetbrains.annotations.NotNull;

public interface ArenaRegistry {

    @NotNull CompletableFuture<Void> recordSlot(@NotNull ArenaSlot slot);

    @NotNull CompletableFuture<Optional<ArenaSlot>> find(@NotNull ArenaId arenaId);

    @NotNull CompletableFuture<List<ArenaSlot>> findAvailable(@NotNull ArenaRequirements requirements);

    @NotNull CompletableFuture<Boolean> tryReserve(@NotNull ArenaReservation reservation, @NotNull Duration reservationTtl);

    @NotNull CompletableFuture<Void> updateState(@NotNull ArenaId arenaId, @NotNull ArenaState state);

    @NotNull CompletableFuture<Void> markDisabled(@NotNull ArenaId arenaId);
}
