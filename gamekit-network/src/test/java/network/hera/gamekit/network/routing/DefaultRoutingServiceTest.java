package network.hera.gamekit.network.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionType;
import network.hera.gamekit.network.location.MatchLocation;
import network.hera.gamekit.network.location.MatchLocationState;
import network.hera.gamekit.network.registry.RegisteredServer;
import network.hera.gamekit.network.registry.ServerHeartbeat;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.network.FakeAdmissionStore;
import network.hera.gamekit.testkit.network.FakeMatchLocationRegistry;
import network.hera.gamekit.testkit.network.FakeServerRegistry;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

final class DefaultRoutingServiceTest {

    @Test
    void routesNewAdmissionToAvailableArenaServer() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArena(ServerState.ONLINE);

        final RoutingDecision decision = runtime.routing.route(newJoinRequest(runtime.clock)).join();

        assertTrue(decision.accepted());
        assertEquals(GameKitFixtures.bedwarsArena01(), decision.requireServerId());
        assertTrue(runtime.admissions.contains(decision.requireAdmissionRequest().id()));
    }

    @Test
    void rejectsNewRoutingWhenOnlyDrainingServerExists() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArena(ServerState.DRAINING);

        final RoutingDecision decision = runtime.routing.route(newJoinRequest(runtime.clock)).join();

        assertTrue(decision.rejected());
        assertEquals(RoutingRejectReason.NO_SERVER_AVAILABLE, decision.rejectReason().orElseThrow());
    }

    @Test
    void reconnectCanRouteToDrainingServerWhenMatchIsAliveThere() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArena(ServerState.DRAINING);
        runtime.locations.save(new MatchLocation(
            GameKitFixtures.matchOne(),
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.lighthouse01(),
            MatchLocationState.RUNNING
        )).join();

        final RoutingDecision decision = runtime.routing.route(newReconnectRequest(runtime.clock)).join();

        assertTrue(decision.accepted());
        assertEquals(GameKitFixtures.bedwarsArena01(), decision.requireServerId());
        assertEquals(GameKitFixtures.lighthouse01(), decision.requireAdmissionRequest().arenaIdOptional().orElseThrow());
    }

    @Test
    void reconnectRejectsMatchLocationFromAnotherGame() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArena(ServerState.ONLINE);
        runtime.locations.save(new MatchLocation(
            GameKitFixtures.matchOne(),
            GameId.of("skywars"),
            GameKitFixtures.casual2v2(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.lighthouse01(),
            MatchLocationState.RUNNING
        )).join();

        final RoutingDecision decision = runtime.routing.route(newReconnectRequest(runtime.clock)).join();

        assertTrue(decision.rejected());
        assertEquals(RoutingRejectReason.MATCH_LOCATION_MISMATCH, decision.rejectReason().orElseThrow());
    }

    @Test
    void preferredServerMustBelongToRequestedGameAndRole() {
        final TestRuntime runtime = TestRuntime.create();
        final ServerId skywarsArena = ServerId.of("skywars-arena-01");
        runtime.servers.recordHeartbeat(new ServerHeartbeat(
            skywarsArena,
            GameId.of("skywars"),
            ServerRole.ARENA,
            ServerState.ONLINE,
            100,
            12,
            runtime.clock.now(),
            Duration.ofSeconds(10)
        )).join();

        final RoutingDecision decision = runtime.routing.route(new RoutingRequest(
            GameKitFixtures.playerOne(),
            AdmissionType.JOIN_WAITING_ROOM,
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            ServerRole.ARENA,
            skywarsArena,
            null,
            runtime.clock.now(),
            Duration.ofSeconds(30)
        )).join();

        assertTrue(decision.rejected());
        assertEquals(RoutingRejectReason.TARGET_SERVER_NOT_AVAILABLE, decision.rejectReason().orElseThrow());
    }

    private static RoutingRequest newJoinRequest(final FakeGameKitClock clock) {
        return new RoutingRequest(
            GameKitFixtures.playerOne(),
            AdmissionType.JOIN_WAITING_ROOM,
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            ServerRole.ARENA,
            null,
            null,
            clock.now(),
            Duration.ofSeconds(30)
        );
    }

    private static RoutingRequest newReconnectRequest(final FakeGameKitClock clock) {
        return new RoutingRequest(
            GameKitFixtures.playerOne(),
            AdmissionType.RECONNECT_MATCH,
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            ServerRole.ARENA,
            null,
            GameKitFixtures.matchOne(),
            clock.now(),
            Duration.ofSeconds(30)
        );
    }

    private record TestRuntime(
            FakeGameKitClock clock,
            FakeServerRegistry servers,
            FakeAdmissionStore admissions,
            FakeMatchLocationRegistry locations,
            DefaultRoutingService routing
    ) {

        static TestRuntime create() {
            final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
            final FakeServerRegistry servers = new FakeServerRegistry(clock);
            final FakeAdmissionStore admissions = new FakeAdmissionStore(clock);
            final FakeMatchLocationRegistry locations = new FakeMatchLocationRegistry();
            return new TestRuntime(clock, servers, admissions, locations, new DefaultRoutingService(servers, admissions, locations));
        }

        void recordArena(final ServerState state) {
            this.servers.recordHeartbeat(new ServerHeartbeat(
                GameKitFixtures.bedwarsArena01(),
                GameKitFixtures.bedwars(),
                ServerRole.ARENA,
                state,
                100,
                12,
                this.clock.now(),
                Duration.ofSeconds(10)
            )).join();
        }
    }
}
