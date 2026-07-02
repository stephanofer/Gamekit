package network.hera.gamekit.infra.craftkit.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Set;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import org.junit.jupiter.api.Test;

final class ArenaRedisPayloadsTest {

    @Test
    void roundTripsArenaSlot() {
        final ArenaSlot slot = new ArenaSlot(
            GameKitFixtures.lighthouse01(),
            "lighthouse",
            GameKitFixtures.bedwars(),
            GameKitFixtures.bedwarsArena01(),
            "lighthouse_world",
            ArenaState.AVAILABLE,
            Set.of("bedwars", "2v2", "ranked_ready"),
            4,
            GameKitFixtures.BASE_TIME,
            Duration.ofSeconds(10)
        );

        final ArenaSlot decoded = ArenaRedisPayloads.slot(ArenaRedisPayloads.slot(slot));

        assertEquals(slot, decoded);
    }

    @Test
    void roundTripsArenaReservation() {
        final ArenaReservation reservation = new ArenaReservation(
            GameKitFixtures.lighthouse01(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            GameKitFixtures.BASE_TIME,
            GameKitFixtures.BASE_TIME.plusSeconds(30)
        );

        final ArenaReservation decoded = ArenaRedisPayloads.reservation(ArenaRedisPayloads.reservation(reservation));

        assertEquals(reservation, decoded);
    }
}
