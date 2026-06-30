package network.hera.gamekit.network.location;

import java.util.Objects;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;

public record MatchLocation(
        @NotNull MatchId matchId,
        @NotNull GameId gameId,
        @NotNull VariantId variantId,
        @NotNull ServerId serverId,
        @NotNull ArenaId arenaId,
        @NotNull MatchLocationState state
) {

    public MatchLocation {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(arenaId, "arenaId");
        Objects.requireNonNull(state, "state");
    }
}
