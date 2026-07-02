package network.hera.gamekit.arena.allocation;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;

public record ArenaReservation(
        @NotNull ArenaId arenaId,
        @NotNull ServerId serverId,
        @NotNull GameId gameId,
        @NotNull VariantId variantId,
        @NotNull Instant reservedAt,
        @NotNull Instant expiresAt
) {

    public ArenaReservation {
        Objects.requireNonNull(arenaId, "arenaId");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(reservedAt, "reservedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(reservedAt)) {
            throw new IllegalArgumentException("Arena reservation expiresAt must be after reservedAt.");
        }
    }
}
