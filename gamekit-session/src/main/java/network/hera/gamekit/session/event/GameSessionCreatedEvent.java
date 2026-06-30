package network.hera.gamekit.session.event;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.event.GameKitEvent;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.session.state.GameSessionState;
import org.jetbrains.annotations.NotNull;

public record GameSessionCreatedEvent(
        @NotNull PlayerId playerId,
        @NotNull GameId gameId,
        @NotNull GameSessionState state,
        @NotNull Instant occurredAt
) implements GameKitEvent {

    public GameSessionCreatedEvent {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
