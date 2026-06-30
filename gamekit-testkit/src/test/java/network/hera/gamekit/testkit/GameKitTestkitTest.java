package network.hera.gamekit.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import network.hera.gamekit.core.definition.MatchKind;
import network.hera.gamekit.core.event.GameKitEvent;
import network.hera.gamekit.testkit.event.RecordingEventBus;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

class GameKitTestkitTest {

    @Test
    void fakeClockAdvancesWithoutSleeping() {
        FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);

        clock.advance(Duration.ofSeconds(30));

        assertEquals(GameKitFixtures.BASE_TIME.plusSeconds(30), clock.now());
    }

    @Test
    void recordingEventBusStoresEventsInOrder() {
        RecordingEventBus eventBus = new RecordingEventBus();
        GameKitEvent first = () -> GameKitFixtures.BASE_TIME;
        GameKitEvent second = () -> GameKitFixtures.BASE_TIME.plusSeconds(1);

        eventBus.publish(first);
        eventBus.publish(second);

        assertEquals(first, eventBus.events().get(0));
        assertEquals(second, eventBus.events().get(1));
    }

    @Test
    void fixturesProduceValidObjects() {
        assertEquals("bedwars", GameKitFixtures.bedwars().toString());
        assertEquals("bedwars:casual_2v2", GameKitFixtures.bedwarsCasual2v2Queue().toString());
        assertEquals("bedwars-lobby-01", GameKitFixtures.bedwarsLobby01().toString());
        assertEquals(MatchKind.CASUAL, GameKitFixtures.casual2v2Variant().kind());
        assertTrue(GameKitFixtures.playerOne().value().toString().endsWith("0001"));
    }
}
