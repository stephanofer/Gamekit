package network.hera.gamekit.queue.casual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.arena.allocation.DefaultArenaAllocationService;
import network.hera.gamekit.core.definition.MatchKind;
import network.hera.gamekit.core.definition.MatchVariant;
import network.hera.gamekit.core.definition.TeamSpec;
import network.hera.gamekit.network.admission.WaitingRoomId;
import network.hera.gamekit.network.registry.ServerHeartbeat;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;
import network.hera.gamekit.network.routing.DefaultRoutingService;
import network.hera.gamekit.network.routing.RoutingDecision;
import network.hera.gamekit.network.routing.RoutingRejectReason;
import network.hera.gamekit.network.routing.RoutingRequest;
import network.hera.gamekit.network.routing.RoutingService;
import network.hera.gamekit.queue.definition.QueueDefinition;
import network.hera.gamekit.queue.room.CasualWaitingRoomView;
import network.hera.gamekit.queue.room.InMemoryCasualWaitingRoomDirectory;
import network.hera.gamekit.queue.ticket.InMemoryQueueTicketStore;
import network.hera.gamekit.session.activeplay.ActivePlayResolver;
import network.hera.gamekit.session.activeplay.ActivePlaySession;
import network.hera.gamekit.session.activeplay.ActivePlayStore;
import network.hera.gamekit.session.activeplay.InMemoryActivePlayStore;
import network.hera.gamekit.testkit.arena.FakeArenaRegistry;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.network.FakeAdmissionStore;
import network.hera.gamekit.testkit.network.FakeMatchLocationRegistry;
import network.hera.gamekit.testkit.network.FakeServerRegistry;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

final class CasualQueueResolverTest {

    @Test
    void routesToExistingJoinableRoomBeforeReservingArena() {
        final TestRuntime runtime = TestRuntime.create();
        final WaitingRoomId waitingRoomId = WaitingRoomId.of("room_01");
        runtime.recordArenaServer(ServerState.ONLINE);
        runtime.rooms.record(new CasualWaitingRoomView(
            waitingRoomId,
            GameKitFixtures.lighthouse01(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwarsCasual2v2Queue(),
            1,
            4,
            runtime.clock.now().plusSeconds(30)
        ));
        runtime.recordSlot(ArenaState.AVAILABLE);

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.ROUTE_EXISTING_ROOM, decision.type());
        assertEquals(waitingRoomId, decision.requireAdmissionRequest().waitingRoomIdOptional().orElseThrow());
        assertEquals(GameKitFixtures.lighthouse01(), decision.requireAdmissionRequest().arenaIdOptional().orElseThrow());
        assertTrue(runtime.activePlay.findByPlayer(GameKitFixtures.playerOne()).orElseThrow().referenceId().equals(waitingRoomId.toString()));
        assertTrue(runtime.arenas.find(GameKitFixtures.lighthouse01()).join().orElseThrow().state() == ArenaState.AVAILABLE);
    }

    @Test
    void choosesFullerJoinableRoomToAvoidFragmentation() {
        final TestRuntime runtime = TestRuntime.create();
        final WaitingRoomId emptierRoom = WaitingRoomId.of("room_01");
        final WaitingRoomId fullerRoom = WaitingRoomId.of("room_02");
        runtime.recordArenaServer(ServerState.ONLINE);
        runtime.rooms.record(new CasualWaitingRoomView(
            emptierRoom,
            GameKitFixtures.lighthouse01(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwarsCasual2v2Queue(),
            1,
            4,
            runtime.clock.now().plusSeconds(30)
        ));
        runtime.rooms.record(new CasualWaitingRoomView(
            fullerRoom,
            GameKitFixtures.rooftop01(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwarsCasual2v2Queue(),
            3,
            4,
            runtime.clock.now().plusSeconds(30)
        ));

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.ROUTE_EXISTING_ROOM, decision.type());
        assertEquals(fullerRoom, decision.requireAdmissionRequest().waitingRoomIdOptional().orElseThrow());
        assertEquals(GameKitFixtures.rooftop01(), decision.requireAdmissionRequest().arenaIdOptional().orElseThrow());
    }

    @Test
    void reservesArenaWhenNoJoinableRoomExists() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArenaServer(ServerState.ONLINE);
        runtime.recordSlot(ArenaState.AVAILABLE);

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.RESERVED_ARENA, decision.type());
        assertEquals(GameKitFixtures.lighthouse01(), decision.requireArenaReservation().arenaId());
        assertEquals(GameKitFixtures.lighthouse01(), decision.requireAdmissionRequest().arenaIdOptional().orElseThrow());
        assertTrue(decision.requireAdmissionRequest().waitingRoomIdOptional().isPresent());
        assertEquals(ArenaState.RESERVED, runtime.arenas.find(GameKitFixtures.lighthouse01()).join().orElseThrow().state());
        assertTrue(runtime.activePlay.findByPlayer(GameKitFixtures.playerOne()).isPresent());
    }

    @Test
    void joinsQueueWhenNoRoomAndNoArenaAvailable() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordArenaServer(ServerState.ONLINE);

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.JOINED_QUEUE, decision.type());
        assertEquals(GameKitFixtures.bedwarsCasual2v2Queue(), decision.requireTicket().queueId());
        assertTrue(runtime.tickets.findByPlayer(GameKitFixtures.playerOne()).isPresent());
        assertEquals(GameKitFixtures.bedwarsCasual2v2Queue().toString(), runtime.activePlay.findByPlayer(GameKitFixtures.playerOne()).orElseThrow().referenceId());
    }

    @Test
    void rejectsWhenPlayerAlreadyHasActivePlay() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.activePlay.putIfAbsent(ActivePlaySession.matchRunning(
            GameKitFixtures.playerOne(),
            GameKitFixtures.bedwars(),
            GameKitFixtures.matchOne().toString(),
            runtime.clock.now()
        ));

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertTrue(decision.rejected());
        assertEquals(CasualQueueRejectReason.ALREADY_IN_MATCH, decision.requireRejectReason());
        assertTrue(runtime.tickets.findByPlayer(GameKitFixtures.playerOne()).isEmpty());
    }

    @Test
    void rejectsNonCasualQueue() {
        final TestRuntime runtime = TestRuntime.create();

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), rankedQueue())).join();

        assertTrue(decision.rejected());
        assertEquals(CasualQueueRejectReason.QUEUE_NOT_CASUAL, decision.requireRejectReason());
    }

    @Test
    void doesNotDuplicateTicketForSamePlayer() {
        final TestRuntime runtime = TestRuntime.create();

        final CasualQueueDecision first = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();
        final CasualQueueDecision second = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.JOINED_QUEUE, first.type());
        assertTrue(second.rejected());
        assertEquals(CasualQueueRejectReason.ALREADY_IN_QUEUE, second.requireRejectReason());
        assertEquals(1, runtime.tickets.findByQueue(GameKitFixtures.bedwarsCasual2v2Queue()).size());
    }

    @Test
    void joinsQueueWhenReservedArenaRoutingFails() {
        final TestRuntime runtime = TestRuntime.create(new RejectingRoutingService());
        runtime.recordArenaServer(ServerState.ONLINE);
        runtime.recordSlot(ArenaState.AVAILABLE);

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertEquals(CasualQueueDecisionType.JOINED_QUEUE, decision.type());
        assertTrue(runtime.tickets.findByPlayer(GameKitFixtures.playerOne()).isPresent());
        assertEquals(ArenaState.RESERVED, runtime.arenas.find(GameKitFixtures.lighthouse01()).join().orElseThrow().state());
    }

    @Test
    void rollsBackTicketWhenActivePlayClaimFailsAfterJoinQueue() {
        final ActivePlayStore activePlay = new ActivePlayStore() {
            @Override
            public @NotNull Optional<ActivePlaySession> findByPlayer(@NotNull network.hera.gamekit.core.id.PlayerId playerId) {
                return Optional.empty();
            }

            @Override
            public boolean putIfAbsent(@NotNull ActivePlaySession session) {
                return false;
            }

            @Override
            public void remove(@NotNull network.hera.gamekit.core.id.PlayerId playerId) {
            }
        };
        final TestRuntime runtime = TestRuntime.create(activePlay, new RejectingRoutingService());

        final CasualQueueDecision decision = runtime.resolver.resolve(new CasualQueueRequest(GameKitFixtures.playerOne(), casualQueue())).join();

        assertTrue(decision.rejected());
        assertEquals(CasualQueueRejectReason.ACTIVE_PLAY_CLAIM_FAILED, decision.requireRejectReason());
        assertTrue(runtime.tickets.findByPlayer(GameKitFixtures.playerOne()).isEmpty());
    }

    private static QueueDefinition casualQueue() {
        return new QueueDefinition(
            GameKitFixtures.bedwarsCasual2v2Queue(),
            GameKitFixtures.casual2v2Variant(),
            new ArenaRequirements(GameKitFixtures.bedwars(), GameKitFixtures.casual2v2(), Set.of("bedwars", "2v2"), 4),
            2,
            Duration.ofSeconds(30),
            Duration.ofSeconds(30)
        );
    }

    private static QueueDefinition rankedQueue() {
        return new QueueDefinition(
            GameKitFixtures.bedwarsCasual2v2Queue(),
            MatchVariant.of(GameKitFixtures.bedwars(), GameKitFixtures.casual2v2(), MatchKind.RANKED, TeamSpec.of(2, 2)),
            new ArenaRequirements(GameKitFixtures.bedwars(), GameKitFixtures.casual2v2(), Set.of("bedwars", "2v2"), 4),
            2,
            Duration.ofSeconds(30),
            Duration.ofSeconds(30)
        );
    }

    private static final class RejectingRoutingService implements RoutingService {

        @Override
        public @NotNull CompletableFuture<RoutingDecision> route(@NotNull RoutingRequest request) {
            return CompletableFuture.completedFuture(RoutingDecision.reject(RoutingRejectReason.NO_SERVER_AVAILABLE));
        }
    }

    private record TestRuntime(
            FakeGameKitClock clock,
            FakeArenaRegistry arenas,
            FakeServerRegistry servers,
            ActivePlayStore activePlay,
            InMemoryQueueTicketStore tickets,
            InMemoryCasualWaitingRoomDirectory rooms,
            CasualQueueResolver resolver
    ) {

        static TestRuntime create() {
            final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
            final FakeServerRegistry servers = new FakeServerRegistry(clock);
            final FakeAdmissionStore admissions = new FakeAdmissionStore(clock);
            return create(new InMemoryActivePlayStore(), new DefaultRoutingService(servers, admissions, new FakeMatchLocationRegistry()), clock, servers);
        }

        static TestRuntime create(final RoutingService routingService) {
            final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
            final FakeServerRegistry servers = new FakeServerRegistry(clock);
            return create(new InMemoryActivePlayStore(), routingService, clock, servers);
        }

        static TestRuntime create(final ActivePlayStore activePlay, final RoutingService routingService) {
            final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
            final FakeServerRegistry servers = new FakeServerRegistry(clock);
            return create(activePlay, routingService, clock, servers);
        }

        private static TestRuntime create(
                final ActivePlayStore activePlay,
                final RoutingService routingService,
                final FakeGameKitClock clock,
                final FakeServerRegistry servers
        ) {
            final FakeArenaRegistry arenas = new FakeArenaRegistry(clock);
            final InMemoryQueueTicketStore tickets = new InMemoryQueueTicketStore();
            final InMemoryCasualWaitingRoomDirectory rooms = new InMemoryCasualWaitingRoomDirectory();
            final CasualQueueResolver resolver = new CasualQueueResolver(
                new ActivePlayResolver(activePlay),
                activePlay,
                tickets,
                rooms,
                new DefaultArenaAllocationService(arenas, servers, clock),
                routingService,
                clock
            );
            return new TestRuntime(clock, arenas, servers, activePlay, tickets, rooms, resolver);
        }

        void recordArenaServer(final ServerState state) {
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

        void recordSlot(final ArenaState state) {
            this.arenas.recordSlot(new ArenaSlot(
                GameKitFixtures.lighthouse01(),
                "lighthouse",
                GameKitFixtures.bedwars(),
                GameKitFixtures.bedwarsArena01(),
                "lighthouse_world",
                state,
                Set.of("bedwars", "2v2"),
                4,
                this.clock.now(),
                Duration.ofSeconds(10)
            )).join();
        }
    }
}
