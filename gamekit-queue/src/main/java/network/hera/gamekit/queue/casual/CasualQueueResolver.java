package network.hera.gamekit.queue.casual;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.arena.allocation.ArenaAllocationRequest;
import network.hera.gamekit.arena.allocation.ArenaAllocationResult;
import network.hera.gamekit.arena.allocation.ArenaAllocationService;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.core.definition.MatchKind;
import network.hera.gamekit.core.result.Decision;
import network.hera.gamekit.core.time.GameKitClock;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.network.admission.AdmissionType;
import network.hera.gamekit.network.admission.WaitingRoomId;
import network.hera.gamekit.network.registry.ServerRole;
import network.hera.gamekit.network.routing.RoutingDecision;
import network.hera.gamekit.network.routing.RoutingRequest;
import network.hera.gamekit.network.routing.RoutingService;
import network.hera.gamekit.queue.definition.QueueDefinition;
import network.hera.gamekit.queue.room.CasualWaitingRoomView;
import network.hera.gamekit.queue.room.CasualWaitingRoomDirectory;
import network.hera.gamekit.queue.ticket.QueueTicket;
import network.hera.gamekit.queue.ticket.QueueTicketStore;
import network.hera.gamekit.session.activeplay.ActivePlayRejectReason;
import network.hera.gamekit.session.activeplay.ActivePlayResolver;
import network.hera.gamekit.session.activeplay.ActivePlaySession;
import network.hera.gamekit.session.activeplay.ActivePlayStore;
import org.jetbrains.annotations.NotNull;

public final class CasualQueueResolver {

    private final ActivePlayResolver activePlayResolver;
    private final ActivePlayStore activePlayStore;
    private final QueueTicketStore ticketStore;
    private final CasualWaitingRoomDirectory waitingRooms;
    private final ArenaAllocationService arenaAllocation;
    private final RoutingService routingService;
    private final GameKitClock clock;

    public CasualQueueResolver(
            @NotNull ActivePlayResolver activePlayResolver,
            @NotNull ActivePlayStore activePlayStore,
            @NotNull QueueTicketStore ticketStore,
            @NotNull CasualWaitingRoomDirectory waitingRooms,
            @NotNull ArenaAllocationService arenaAllocation,
            @NotNull RoutingService routingService,
            @NotNull GameKitClock clock
    ) {
        this.activePlayResolver = Objects.requireNonNull(activePlayResolver, "activePlayResolver");
        this.activePlayStore = Objects.requireNonNull(activePlayStore, "activePlayStore");
        this.ticketStore = Objects.requireNonNull(ticketStore, "ticketStore");
        this.waitingRooms = Objects.requireNonNull(waitingRooms, "waitingRooms");
        this.arenaAllocation = Objects.requireNonNull(arenaAllocation, "arenaAllocation");
        this.routingService = Objects.requireNonNull(routingService, "routingService");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public @NotNull CompletableFuture<CasualQueueDecision> resolve(@NotNull CasualQueueRequest request) {
        Objects.requireNonNull(request, "request");
        final QueueDefinition queue = request.queue();
        if (queue.variant().kind() != MatchKind.CASUAL) {
            return CompletableFuture.completedFuture(CasualQueueDecision.reject(CasualQueueRejectReason.QUEUE_NOT_CASUAL));
        }
        final Decision<ActivePlayRejectReason> activePlay = this.activePlayResolver.resolve(request.playerId());
        if (activePlay.rejected()) {
            return CompletableFuture.completedFuture(CasualQueueDecision.reject(mapActivePlay(activePlay.requireRejectReason())));
        }
        return this.waitingRooms.findJoinable(queue, this.clock.now())
            .map(room -> routeExistingRoom(request, room))
            .orElseGet(() -> reserveArenaOrJoinQueue(request));
    }

    private @NotNull CompletableFuture<CasualQueueDecision> routeExistingRoom(
            @NotNull CasualQueueRequest request,
            @NotNull CasualWaitingRoomView room
    ) {
        return this.routingService.route(new RoutingRequest(
            request.playerId(),
            AdmissionType.JOIN_WAITING_ROOM,
            request.queue().queueId().gameId(),
            request.queue().queueId().variantId(),
            ServerRole.ARENA,
            room.serverId(),
            room.arenaId(),
            room.waitingRoomId(),
            null,
            this.clock.now(),
            request.queue().admissionTtl()
        )).thenApply(decision -> decision.accepted()
            ? claimWaitingRoomOrReject(request, decision.requireAdmissionRequest(), room.waitingRoomId())
            : joinQueueAfterRoutingFailure(request));
    }

    private @NotNull CompletableFuture<CasualQueueDecision> reserveArenaOrJoinQueue(@NotNull CasualQueueRequest request) {
        return this.arenaAllocation.tryReserve(new ArenaAllocationRequest(
            request.queue().arenaRequirements(),
            this.clock.now(),
            request.queue().arenaReservationTtl()
        )).thenCompose(allocation -> allocation.accepted()
            ? routeReservedArena(request, allocation)
            : CompletableFuture.completedFuture(joinQueue(request)));
    }

    private @NotNull CompletableFuture<CasualQueueDecision> routeReservedArena(
            @NotNull CasualQueueRequest request,
            @NotNull ArenaAllocationResult allocation
    ) {
        final ArenaReservation reservation = allocation.requireReservation();
        final WaitingRoomId waitingRoomId = WaitingRoomId.of("wr_" + UUID.randomUUID().toString().replace("-", ""));
        return this.routingService.route(new RoutingRequest(
            request.playerId(),
            AdmissionType.JOIN_WAITING_ROOM,
            request.queue().queueId().gameId(),
            request.queue().queueId().variantId(),
            ServerRole.ARENA,
            reservation.serverId(),
            reservation.arenaId(),
            waitingRoomId,
            null,
            this.clock.now(),
            request.queue().admissionTtl()
        )).thenApply(decision -> decision.accepted()
            ? claimReservedArenaOrReject(request, decision.requireAdmissionRequest(), reservation, waitingRoomId)
            : joinQueueAfterRoutingFailure(request));
    }

    private @NotNull CasualQueueDecision claimWaitingRoomOrReject(
            @NotNull CasualQueueRequest request,
            @NotNull AdmissionRequest admission,
            @NotNull WaitingRoomId waitingRoomId
    ) {
        final boolean claimed = this.activePlayStore.putIfAbsent(ActivePlaySession.waitingRoom(
            request.playerId(),
            request.queue().queueId().gameId(),
            waitingRoomId.toString(),
            this.clock.now()
        ));
        return claimed
            ? CasualQueueDecision.routeExistingRoom(admission)
            : CasualQueueDecision.reject(CasualQueueRejectReason.ACTIVE_PLAY_CLAIM_FAILED);
    }

    private @NotNull CasualQueueDecision claimReservedArenaOrReject(
            @NotNull CasualQueueRequest request,
            @NotNull AdmissionRequest admission,
            @NotNull ArenaReservation reservation,
            @NotNull WaitingRoomId waitingRoomId
    ) {
        final boolean claimed = this.activePlayStore.putIfAbsent(ActivePlaySession.waitingRoom(
            request.playerId(),
            request.queue().queueId().gameId(),
            waitingRoomId.toString(),
            this.clock.now()
        ));
        return claimed
            ? CasualQueueDecision.reservedArena(admission, reservation)
            : CasualQueueDecision.reject(CasualQueueRejectReason.ACTIVE_PLAY_CLAIM_FAILED);
    }

    private @NotNull CasualQueueDecision joinQueueAfterRoutingFailure(@NotNull CasualQueueRequest request) {
        return joinQueue(request);
    }

    private @NotNull CasualQueueDecision joinQueue(@NotNull CasualQueueRequest request) {
        final QueueTicket ticket = QueueTicket.solo(request.queue().queueId(), request.playerId(), this.clock.now());
        if (!this.ticketStore.putIfAbsent(ticket)) {
            return CasualQueueDecision.reject(CasualQueueRejectReason.ALREADY_IN_QUEUE);
        }
        final boolean claimed = this.activePlayStore.putIfAbsent(ActivePlaySession.queue(request.playerId(), request.queue().queueId(), this.clock.now()));
        if (!claimed) {
            this.ticketStore.remove(request.playerId());
            return CasualQueueDecision.reject(CasualQueueRejectReason.ACTIVE_PLAY_CLAIM_FAILED);
        }
        return CasualQueueDecision.joinedQueue(ticket);
    }

    private static @NotNull CasualQueueRejectReason mapActivePlay(@NotNull ActivePlayRejectReason reason) {
        return switch (reason) {
            case ALREADY_IN_QUEUE -> CasualQueueRejectReason.ALREADY_IN_QUEUE;
            case ALREADY_IN_WAITING_ROOM -> CasualQueueRejectReason.ALREADY_IN_WAITING_ROOM;
            case ALREADY_IN_MATCH -> CasualQueueRejectReason.ALREADY_IN_MATCH;
            case ALREADY_SPECTATING -> CasualQueueRejectReason.ALREADY_SPECTATING;
            case RECONNECTABLE_MATCH_EXISTS -> CasualQueueRejectReason.RECONNECTABLE_MATCH_EXISTS;
        };
    }
}
