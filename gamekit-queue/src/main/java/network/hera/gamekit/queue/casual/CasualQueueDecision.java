package network.hera.gamekit.queue.casual;

import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.arena.allocation.ArenaReservation;
import network.hera.gamekit.network.admission.AdmissionRequest;
import network.hera.gamekit.queue.ticket.QueueTicket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CasualQueueDecision {

    private final CasualQueueDecisionType type;
    private final @Nullable CasualQueueRejectReason rejectReason;
    private final @Nullable QueueTicket ticket;
    private final @Nullable AdmissionRequest admissionRequest;
    private final @Nullable ArenaReservation arenaReservation;

    private CasualQueueDecision(
            @NotNull CasualQueueDecisionType type,
            @Nullable CasualQueueRejectReason rejectReason,
            @Nullable QueueTicket ticket,
            @Nullable AdmissionRequest admissionRequest,
            @Nullable ArenaReservation arenaReservation
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.rejectReason = rejectReason;
        this.ticket = ticket;
        this.admissionRequest = admissionRequest;
        this.arenaReservation = arenaReservation;
    }

    public static @NotNull CasualQueueDecision reject(@NotNull CasualQueueRejectReason reason) {
        return new CasualQueueDecision(CasualQueueDecisionType.REJECTED, Objects.requireNonNull(reason, "reason"), null, null, null);
    }

    public static @NotNull CasualQueueDecision joinedQueue(@NotNull QueueTicket ticket) {
        return new CasualQueueDecision(CasualQueueDecisionType.JOINED_QUEUE, null, Objects.requireNonNull(ticket, "ticket"), null, null);
    }

    public static @NotNull CasualQueueDecision routeExistingRoom(@NotNull AdmissionRequest admissionRequest) {
        return new CasualQueueDecision(CasualQueueDecisionType.ROUTE_EXISTING_ROOM, null, null, Objects.requireNonNull(admissionRequest, "admissionRequest"), null);
    }

    public static @NotNull CasualQueueDecision reservedArena(@NotNull AdmissionRequest admissionRequest, @NotNull ArenaReservation reservation) {
        return new CasualQueueDecision(CasualQueueDecisionType.RESERVED_ARENA, null, null, Objects.requireNonNull(admissionRequest, "admissionRequest"), Objects.requireNonNull(reservation, "reservation"));
    }

    public @NotNull CasualQueueDecisionType type() {
        return this.type;
    }

    public boolean accepted() {
        return this.type != CasualQueueDecisionType.REJECTED;
    }

    public boolean rejected() {
        return this.type == CasualQueueDecisionType.REJECTED;
    }

    public @NotNull Optional<CasualQueueRejectReason> rejectReason() {
        return Optional.ofNullable(this.rejectReason);
    }

    public @NotNull CasualQueueRejectReason requireRejectReason() {
        if (this.rejectReason == null) {
            throw new IllegalStateException("Casual queue decision was accepted.");
        }
        return this.rejectReason;
    }

    public @NotNull Optional<QueueTicket> ticket() {
        return Optional.ofNullable(this.ticket);
    }

    public @NotNull QueueTicket requireTicket() {
        if (this.ticket == null) {
            throw new IllegalStateException("Casual queue decision has no ticket.");
        }
        return this.ticket;
    }

    public @NotNull Optional<AdmissionRequest> admissionRequest() {
        return Optional.ofNullable(this.admissionRequest);
    }

    public @NotNull AdmissionRequest requireAdmissionRequest() {
        if (this.admissionRequest == null) {
            throw new IllegalStateException("Casual queue decision has no admission request.");
        }
        return this.admissionRequest;
    }

    public @NotNull Optional<ArenaReservation> arenaReservation() {
        return Optional.ofNullable(this.arenaReservation);
    }

    public @NotNull ArenaReservation requireArenaReservation() {
        if (this.arenaReservation == null) {
            throw new IllegalStateException("Casual queue decision has no arena reservation.");
        }
        return this.arenaReservation;
    }
}
