package network.hera.gamekit.arena.allocation;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.arena.ArenaRequirements;
import org.jetbrains.annotations.NotNull;

public record ArenaAllocationRequest(
        @NotNull ArenaRequirements requirements,
        @NotNull Instant requestedAt,
        @NotNull Duration reservationTtl
) {

    public ArenaAllocationRequest {
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(requestedAt, "requestedAt");
        Objects.requireNonNull(reservationTtl, "reservationTtl");
        if (reservationTtl.isZero() || reservationTtl.isNegative()) {
            throw new IllegalArgumentException("Arena reservation TTL must be positive.");
        }
    }
}
