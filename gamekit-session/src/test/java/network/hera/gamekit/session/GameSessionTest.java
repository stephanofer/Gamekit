package network.hera.gamekit.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import network.hera.gamekit.core.error.InvalidGameKitStateException;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.session.event.GameSessionCreatedEvent;
import network.hera.gamekit.session.event.GameSessionStateChangedEvent;
import network.hera.gamekit.session.state.GameSessionState;
import network.hera.gamekit.session.transition.GameSessionTransitionRejectReason;
import network.hera.gamekit.testkit.event.RecordingEventBus;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    @Test
    void createsSessionInLobbyAndPublishesEvent() {
        FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        RecordingEventBus eventBus = new RecordingEventBus();

        GameSession session = GameSession.create(GameKitFixtures.playerOne(), GameKitFixtures.bedwars(), clock, eventBus);

        assertEquals(GameSessionState.LOBBY, session.state());
        assertEquals(GameKitFixtures.BASE_TIME, session.createdAt());
        assertEquals(1, eventBus.eventsOfType(GameSessionCreatedEvent.class).size());
    }

    @Test
    void validTransitionChangesStateAndPublishesEvent() {
        FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        RecordingEventBus eventBus = new RecordingEventBus();
        GameSession session = GameSession.create(GameKitFixtures.playerOne(), GameKitFixtures.bedwars(), clock, eventBus);
        clock.advance(Duration.ofSeconds(5));

        Decision<GameSessionTransitionRejectReason> decision = session.tryTransitionTo(GameSessionState.QUEUE, clock, eventBus);

        assertTrue(decision.accepted());
        assertEquals(GameSessionState.QUEUE, session.state());
        GameSessionStateChangedEvent event = eventBus.eventsOfType(GameSessionStateChangedEvent.class).getFirst();
        assertEquals(GameSessionState.LOBBY, event.previousState());
        assertEquals(GameSessionState.QUEUE, event.newState());
        assertEquals(GameKitFixtures.BASE_TIME.plusSeconds(5), event.occurredAt());
    }

    @Test
    void invalidTransitionIsRejected() {
        FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        RecordingEventBus eventBus = new RecordingEventBus();
        GameSession session = GameSession.create(GameKitFixtures.playerOne(), GameKitFixtures.bedwars(), clock, eventBus);

        Decision<GameSessionTransitionRejectReason> decision = session.tryTransitionTo(GameSessionState.PLAYING, clock, eventBus);

        assertTrue(decision.rejected());
        assertEquals(GameSessionTransitionRejectReason.TRANSITION_NOT_ALLOWED, decision.requireRejectReason());
        assertEquals(GameSessionState.LOBBY, session.state());
    }

    @Test
    void transitionToThrowsForInvalidTransition() {
        FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        RecordingEventBus eventBus = new RecordingEventBus();
        GameSession session = GameSession.create(GameKitFixtures.playerOne(), GameKitFixtures.bedwars(), clock, eventBus);

        assertThrows(InvalidGameKitStateException.class, () -> session.transitionTo(GameSessionState.PLAYING, clock, eventBus));
    }
}
