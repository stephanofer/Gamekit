package network.hera.gamekit.arena.allocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.arena.ArenaSlot;
import network.hera.gamekit.arena.ArenaState;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.registry.ServerHeartbeat;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.registry.ServerState;
import network.hera.gamekit.testkit.arena.FakeArenaRegistry;
import network.hera.gamekit.testkit.fixture.GameKitFixtures;
import network.hera.gamekit.testkit.network.FakeServerRegistry;
import network.hera.gamekit.testkit.time.FakeGameKitClock;
import org.junit.jupiter.api.Test;

final class DefaultArenaAllocationServiceTest {

    @Test
    void reservesCompatibleAvailableArena() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4);

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.accepted());
        assertEquals(GameKitFixtures.lighthouse01(), result.requireReservation().arenaId());
        assertEquals(ArenaState.RESERVED, runtime.arenas.find(GameKitFixtures.lighthouse01()).join().orElseThrow().state());
        assertTrue(runtime.arenas.hasReservation(GameKitFixtures.lighthouse01()));
    }

    @Test
    void rejectsWhenNoCompatibleArenaExists() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "solo"), 4);

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.rejected());
        assertEquals(ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA, result.requireRejectReason());
    }

    @Test
    void rejectsWhenArenaCapacityIsTooSmall() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 2);

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.rejected());
        assertEquals(ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA, result.requireRejectReason());
    }

    @Test
    void ignoresNonReservableArenaStates() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        for (final ArenaState state : List.of(ArenaState.RESERVED, ArenaState.WAITING_ROOM, ArenaState.PLAYING, ArenaState.RESETTING, ArenaState.DISABLED)) {
            runtime.recordSlot(ArenaId.of("arena_" + state.name().toLowerCase()), GameKitFixtures.bedwarsArena01(), state, Set.of("bedwars", "2v2"), 4);
        }

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.rejected());
        assertEquals(ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA, result.requireRejectReason());
    }

    @Test
    void rejectsWhenCompatibleArenaServerDoesNotAcceptNewWork() {
        for (final ServerState state : List.of(ServerState.DRAINING, ServerState.FULL, ServerState.OFFLINE, ServerState.UNKNOWN)) {
            final TestRuntime runtime = TestRuntime.create();
            runtime.recordServer(GameKitFixtures.bedwarsArena01(), state);
            runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4);

            final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

            assertTrue(result.rejected());
            assertEquals(ArenaAllocationRejectReason.NO_SERVER_AVAILABLE, result.requireRejectReason());
        }
    }

    @Test
    void triesNextCandidateWhenFirstIsReservedByAnotherCoordinator() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordServer(GameKitFixtures.bedwarsArena02(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4);
        runtime.recordSlot(GameKitFixtures.rooftop01(), GameKitFixtures.bedwarsArena02(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4);
        runtime.arenas.tryReserve(new ArenaReservation(
            GameKitFixtures.lighthouse01(),
            GameKitFixtures.bedwarsArena01(),
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            runtime.clock.now(),
            runtime.clock.now().plusSeconds(30)
        ), Duration.ofSeconds(30)).join();

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.accepted());
        assertEquals(GameKitFixtures.rooftop01(), result.requireReservation().arenaId());
    }

    @Test
    void concurrentAllocationsDoNotReserveSameArena() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4);

        final CompletableFuture<ArenaAllocationResult> first = CompletableFuture.supplyAsync(() -> runtime.allocation.tryReserve(request(runtime.clock)).join());
        final CompletableFuture<ArenaAllocationResult> second = CompletableFuture.supplyAsync(() -> runtime.allocation.tryReserve(request(runtime.clock)).join());
        final List<ArenaAllocationResult> results = List.of(first.join(), second.join());

        assertEquals(1, results.stream().filter(ArenaAllocationResult::accepted).count());
        assertEquals(1, results.stream().filter(ArenaAllocationResult::rejected).count());
        assertTrue(results.stream()
            .map(result -> result.rejectReason().orElse(null))
            .anyMatch(reason -> reason == ArenaAllocationRejectReason.ARENA_ALREADY_RESERVED
                || reason == ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA));
    }

    @Test
    void expiredSlotIsNotAvailable() {
        final TestRuntime runtime = TestRuntime.create();
        runtime.recordServer(GameKitFixtures.bedwarsArena01(), ServerState.ONLINE);
        runtime.recordSlot(GameKitFixtures.lighthouse01(), GameKitFixtures.bedwarsArena01(), ArenaState.AVAILABLE, Set.of("bedwars", "2v2"), 4, Duration.ofSeconds(1));
        runtime.clock.advance(Duration.ofSeconds(1));

        final ArenaAllocationResult result = runtime.allocation.tryReserve(request(runtime.clock)).join();

        assertTrue(result.rejected());
        assertEquals(ArenaAllocationRejectReason.NO_COMPATIBLE_ARENA, result.requireRejectReason());
        assertTrue(runtime.arenas.find(GameKitFixtures.lighthouse01()).join().isEmpty());
    }

    @Test
    void rejectsInvalidArenaTagFormat() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ArenaRequirements(
            GameKitFixtures.bedwars(),
            GameKitFixtures.casual2v2(),
            Set.of("bedwars,2v2"),
            4
        ));

        assertTrue(exception.getMessage().contains("lowercase_snake_case"));
    }

    private static ArenaAllocationRequest request(final FakeGameKitClock clock) {
        return new ArenaAllocationRequest(
            new ArenaRequirements(GameKitFixtures.bedwars(), GameKitFixtures.casual2v2(), Set.of("bedwars", "2v2"), 4),
            clock.now(),
            Duration.ofSeconds(30)
        );
    }

    private record TestRuntime(
            FakeGameKitClock clock,
            FakeArenaRegistry arenas,
            FakeServerRegistry servers,
            DefaultArenaAllocationService allocation
    ) {

        static TestRuntime create() {
            final FakeGameKitClock clock = FakeGameKitClock.fixedAt(GameKitFixtures.BASE_TIME);
            final FakeArenaRegistry arenas = new FakeArenaRegistry(clock);
            final FakeServerRegistry servers = new FakeServerRegistry(clock);
            return new TestRuntime(clock, arenas, servers, new DefaultArenaAllocationService(arenas, servers, clock));
        }

        void recordServer(final ServerId serverId, final ServerState state) {
            this.servers.recordHeartbeat(new ServerHeartbeat(
                serverId,
                GameKitFixtures.bedwars(),
                ServerRole.ARENA,
                state,
                100,
                state == ServerState.FULL ? 100 : 12,
                this.clock.now(),
                Duration.ofSeconds(10)
            )).join();
        }

        void recordSlot(final ArenaId arenaId, final ServerId serverId, final ArenaState state, final Set<String> tags, final int maxPlayers) {
            recordSlot(arenaId, serverId, state, tags, maxPlayers, Duration.ofSeconds(10));
        }

        void recordSlot(final ArenaId arenaId, final ServerId serverId, final ArenaState state, final Set<String> tags, final int maxPlayers, final Duration ttl) {
            this.arenas.recordSlot(new ArenaSlot(
                arenaId,
                "lighthouse",
                GameKitFixtures.bedwars(),
                serverId,
                arenaId + "_world",
                state,
                tags,
                maxPlayers,
                this.clock.now(),
                ttl
            )).join();
        }
    }
}
