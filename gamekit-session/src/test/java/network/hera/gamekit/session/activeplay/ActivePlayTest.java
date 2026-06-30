package network.hera.gamekit.session.activeplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import org.junit.jupiter.api.Test;

class ActivePlayTest {

    @Test
    void putIfAbsentPreventsDuplicateActivePlay() {
        InMemoryActivePlayStore store = new InMemoryActivePlayStore();
        ActivePlaySession first = ActivePlaySession.queue(
                GameKitFixtures.playerOne(),
                GameKitFixtures.bedwarsCasual2v2Queue(),
                GameKitFixtures.BASE_TIME
        );
        ActivePlaySession second = ActivePlaySession.waitingRoom(
                GameKitFixtures.playerOne(),
                GameKitFixtures.bedwars(),
                "room_01",
                GameKitFixtures.BASE_TIME
        );

        assertTrue(store.putIfAbsent(first));
        assertFalse(store.putIfAbsent(second));
        assertEquals(ActivePlayType.QUEUE, store.findByPlayer(GameKitFixtures.playerOne()).orElseThrow().type());
    }

    @Test
    void resolverAcceptsWhenPlayerHasNoActivePlay() {
        ActivePlayResolver resolver = new ActivePlayResolver(new InMemoryActivePlayStore());

        Decision<ActivePlayRejectReason> decision = resolver.resolve(GameKitFixtures.playerOne());

        assertTrue(decision.accepted());
    }

    @Test
    void resolverRejectsWhenPlayerIsAlreadyInQueue() {
        InMemoryActivePlayStore store = new InMemoryActivePlayStore();
        store.putIfAbsent(ActivePlaySession.queue(
                GameKitFixtures.playerOne(),
                GameKitFixtures.bedwarsCasual2v2Queue(),
                GameKitFixtures.BASE_TIME
        ));
        ActivePlayResolver resolver = new ActivePlayResolver(store);

        Decision<ActivePlayRejectReason> decision = resolver.resolve(GameKitFixtures.playerOne());

        assertTrue(decision.rejected());
        assertEquals(ActivePlayRejectReason.ALREADY_IN_QUEUE, decision.requireRejectReason());
    }

    @Test
    void resolverTreatsReconnectableMatchAsSpecialConflict() {
        InMemoryActivePlayStore store = new InMemoryActivePlayStore();
        store.putIfAbsent(ActivePlaySession.reconnectableMatch(
                GameKitFixtures.playerOne(),
                GameKitFixtures.bedwars(),
                "match_01",
                GameKitFixtures.BASE_TIME,
                GameKitFixtures.BASE_TIME.plusSeconds(60)
        ));
        ActivePlayResolver resolver = new ActivePlayResolver(store);

        Decision<ActivePlayRejectReason> decision = resolver.resolve(GameKitFixtures.playerOne());

        assertTrue(decision.rejected());
        assertEquals(ActivePlayRejectReason.RECONNECTABLE_MATCH_EXISTS, decision.requireRejectReason());
    }
}
