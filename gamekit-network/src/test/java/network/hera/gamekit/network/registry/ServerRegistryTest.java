package network.hera.gamekit.network.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.network.FakeServerRegistry;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

final class ServerRegistryTest {

    @Test
    void expiredHeartbeatMakesServerUnknownAndUnavailable() {
        final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        final FakeServerRegistry registry = new FakeServerRegistry(clock);

        registry.recordHeartbeat(heartbeat(clock, ServerState.ONLINE)).join();

        clock.advance(Duration.ofSeconds(11));

        final RegisteredServer server = registry.find(GameKitFixtures.bedwarsArena01()).join().orElseThrow();
        assertEquals(ServerState.UNKNOWN, server.stateAt(clock.now()));
        assertTrue(registry.findAvailable(GameKitFixtures.bedwars(), ServerRole.ARENA).join().isEmpty());
    }

    @Test
    void drainingServerIsNotAvailableForNewWork() {
        final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        final FakeServerRegistry registry = new FakeServerRegistry(clock);

        registry.recordHeartbeat(heartbeat(clock, ServerState.DRAINING)).join();

        assertTrue(registry.findAvailable(GameKitFixtures.bedwars(), ServerRole.ARENA).join().isEmpty());
    }

    private static ServerHeartbeat heartbeat(final FakeGameKitClock clock, final ServerState state) {
        return new ServerHeartbeat(
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwars(),
            ServerRole.ARENA,
            state,
            100,
            12,
            clock.now(),
            Duration.ofSeconds(10)
        );
    }
}
