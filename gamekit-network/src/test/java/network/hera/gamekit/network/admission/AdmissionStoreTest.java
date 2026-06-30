package network.hera.gamekit.network.admission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.network.FakeAdmissionStore;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

final class AdmissionStoreTest {

    @Test
    void admissionCanBeConsumedOnlyOnce() {
        final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        final FakeAdmissionStore store = new FakeAdmissionStore(clock);
        final AdmissionRequest request = request(clock);

        store.save(request).join();

        assertEquals(request, store.consume(request.id()).join().orElseThrow());
        assertTrue(store.consume(request.id()).join().isEmpty());
    }

    @Test
    void expiredAdmissionCannotBeFoundOrConsumed() {
        final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
        final FakeAdmissionStore store = new FakeAdmissionStore(clock);
        final AdmissionRequest request = request(clock);

        store.save(request).join();
        clock.advance(Duration.ofSeconds(31));

        assertTrue(store.find(request.id()).join().isEmpty());
        assertTrue(store.consume(request.id()).join().isEmpty());
    }

    private static AdmissionRequest request(final FakeGameKitClock clock) {
        return AdmissionRequest.builder()
            .playerId(GameKitFixtures.playerOne())
            .type(AdmissionType.JOIN_WAITING_ROOM)
            .targetServerId(GameKitFixtures.bedwarsArena01())
            .gameId(GameKitFixtures.bedwars())
            .variantId(GameKitFixtures.casual2v2())
            .createdAt(clock.now())
            .expiresAt(clock.now().plusSeconds(30))
            .build();
    }
}
