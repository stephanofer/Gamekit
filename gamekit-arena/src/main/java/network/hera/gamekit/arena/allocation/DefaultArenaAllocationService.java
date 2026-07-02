package network.hera.gamekit.arena.allocation;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.registry.ArenaRegistry;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerRegistry;
import org.jetbrains.annotations.NotNull;

public final class DefaultArenaAllocationService implements ArenaAllocationService {

    private final ArenaRegistry arenaRegistry;
    private final ServerRegistry serverRegistry;
    private final GameKitClock clock;

    public DefaultArenaAllocationService(
            @NotNull ArenaRegistry arenaRegistry,
            @NotNull ServerRegistry serverRegistry,
            @NotNull GameKitClock clock
    ) {
        this.arenaRegistry = Objects.requireNonNull(arenaRegistry, "arenaRegistry");
        this.serverRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public @NotNull CompletableFuture<ArenaAllocationResult> tryReserve(@NotNull ArenaAllocationRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.requirements().requiredPlayers() <= 0) {
            return CompletableFuture.completedFuture(ArenaAllocationResult.reject(ArenaAllocationRejectReason.INVALID_REQUIREMENTS));
        }
        return this.arenaRegistry.findAvailable(request.requirements()).thenCompose(slots -> {
            if (slots.isEmpty()) {
                return CompletableFuture.completedFuture(ArenaAllocationResult.reject(ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA));
            }
            final List<ArenaSlot> sortedSlots = slots.stream()
                .sorted(Comparator.comparing((ArenaSlot slot) -> slot.serverId().toString()).thenComparing(slot -> slot.arenaId().toString()))
                .toList();
            return tryCandidate(request, sortedSlots, 0, false, false);
        });
    }

    private @NotNull CompletableFuture<ArenaAllocationResult> tryCandidate(
            @NotNull ArenaAllocationRequest request,
            @NotNull List<ArenaSlot> slots,
            int index,
            boolean sawUnavailableServer,
            boolean sawReservationConflict
    ) {
        if (index >= slots.size()) {
            if (sawReservationConflict) {
                return CompletableFuture.completedFuture(ArenaAllocationResult.reject(ArenaAllocationRejectReason.ARENA_ALREADY_RESERVED));
            }
            if (sawUnavailableServer) {
                return CompletableFuture.completedFuture(ArenaAllocationResult.reject(ArenaAllocationRejectReason.NO_SERVER_AVAILABLE));
            }
            return CompletableFuture.completedFuture(ArenaAllocationResult.reject(ArenaAllocationRejectReason.RESERVATION_FAILED));
        }

        final ArenaSlot slot = slots.get(index);
        return this.serverRegistry.find(slot.serverId()).thenCompose(server -> {
            if (server.isEmpty() || !server.get().acceptsNewWorkAt(request.requestedAt())) {
                return tryCandidate(request, slots, index + 1, true, sawReservationConflict);
            }
            return reserveCandidate(request, slot, server.get()).thenCompose(result -> {
                if (result.accepted()) {
                    return CompletableFuture.completedFuture(result);
                }
                return tryCandidate(request, slots, index + 1, sawUnavailableServer, true);
            });
        });
    }

    private @NotNull CompletableFuture<ArenaAllocationResult> reserveCandidate(
            @NotNull ArenaAllocationRequest request,
            @NotNull ArenaSlot slot,
            @NotNull RegisteredServer server
    ) {
        final Instant reservedAt = this.clock.now();
        final ArenaReservation reservation = new ArenaReservation(
            slot.arenaId(),
            server.serverId(),
            request.requirements().gameId(),
            request.requirements().variantId(),
            reservedAt,
            reservedAt.plus(request.reservationTtl())
        );
        return this.arenaRegistry.tryReserve(reservation, request.reservationTtl())
            .thenApply(reserved -> reserved
                ? ArenaAllocationResult.accept(reservation)
                : ArenaAllocationResult.reject(ArenaAllocationRejectReason.ARENA_ALREADY_RESERVED));
    }
}
