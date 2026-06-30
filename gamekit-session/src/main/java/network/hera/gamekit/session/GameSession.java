package network.hera.gamekit.session;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.error.InvalidGameKitStateException;
import network.hera.gamekit.core.event.GameKitEventBus;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.session.event.GameSessionCreatedEvent;
import network.hera.gamekit.session.event.GameSessionStateChangedEvent;
import network.hera.gamekit.session.state.GameSessionState;
import network.hera.gamekit.session.transition.GameSessionTransitionRejectReason;
import network.hera.gamekit.session.transition.GameSessionTransitionRules;
import org.jetbrains.annotations.NotNull;

public final class GameSession {

    private final PlayerId playerId;
    private final GameId gameId;
    private final Instant createdAt;
    private GameSessionState state;
    private Instant updatedAt;

    private GameSession(PlayerId playerId, GameId gameId, GameSessionState state, Instant createdAt, Instant updatedAt) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.state = Objects.requireNonNull(state, "state");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static @NotNull GameSession create(
            @NotNull PlayerId playerId,
            @NotNull GameId gameId,
            @NotNull GameKitClock clock,
            @NotNull GameKitEventBus eventBus
    ) {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(eventBus, "eventBus");
        Instant now = clock.now();
        GameSession session = new GameSession(playerId, gameId, GameSessionState.LOBBY, now, now);
        eventBus.publish(new GameSessionCreatedEvent(playerId, gameId, GameSessionState.LOBBY, now));
        return session;
    }

    public @NotNull Decision<GameSessionTransitionRejectReason> tryTransitionTo(
            @NotNull GameSessionState newState,
            @NotNull GameKitClock clock,
            @NotNull GameKitEventBus eventBus
    ) {
        Objects.requireNonNull(newState, "newState");
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(eventBus, "eventBus");

        if (!GameSessionTransitionRules.canTransition(this.state, newState)) {
            return Decision.reject(GameSessionTransitionRejectReason.TRANSITION_NOT_ALLOWED);
        }

        transitionUnchecked(newState, clock.now(), eventBus);
        return Decision.accept();
    }

    public void transitionTo(
            @NotNull GameSessionState newState,
            @NotNull GameKitClock clock,
            @NotNull GameKitEventBus eventBus
    ) {
        Decision<GameSessionTransitionRejectReason> decision = tryTransitionTo(newState, clock, eventBus);
        if (decision.rejected()) {
            throw new InvalidGameKitStateException(this.state.name(), "cannot transition to " + newState.name());
        }
    }

    private void transitionUnchecked(GameSessionState newState, Instant occurredAt, GameKitEventBus eventBus) {
        GameSessionState previousState = this.state;
        this.state = newState;
        this.updatedAt = occurredAt;
        eventBus.publish(new GameSessionStateChangedEvent(this.playerId, this.gameId, previousState, newState, occurredAt));
    }

    public @NotNull PlayerId playerId() {
        return this.playerId;
    }

    public @NotNull GameId gameId() {
        return this.gameId;
    }

    public @NotNull GameSessionState state() {
        return this.state;
    }

    public @NotNull Instant createdAt() {
        return this.createdAt;
    }

    public @NotNull Instant updatedAt() {
        return this.updatedAt;
    }
}
