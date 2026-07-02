package network.hera.gamekit.arena.allocation;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArenaAllocationResult {

    private final @Nullable ArenaReservation reservation;
    private final @Nullable ArenaAllocationRejectReason rejectReason;

    private ArenaAllocationResult(@Nullable ArenaReservation reservation, @Nullable ArenaAllocationRejectReason rejectReason) {
        this.reservation = reservation;
        this.rejectReason = rejectReason;
    }

    public static @NotNull ArenaAllocationResult accept(@NotNull ArenaReservation reservation) {
        return new ArenaAllocationResult(Objects.requireNonNull(reservation, "reservation"), null);
    }

    public static @NotNull ArenaAllocationResult reject(@NotNull ArenaAllocationRejectReason reason) {
        return new ArenaAllocationResult(null, Objects.requireNonNull(reason, "reason"));
    }

    public boolean accepted() {
        return this.reservation != null;
    }

    public boolean rejected() {
        return this.rejectReason != null;
    }

    public @NotNull Optional<ArenaReservation> reservation() {
        return Optional.ofNullable(this.reservation);
    }

    public @NotNull ArenaReservation requireReservation() {
        if (this.reservation == null) {
            throw new IllegalStateException("Arena allocation was rejected");
        }
        return this.reservation;
    }

    public @NotNull Optional<ArenaAllocationRejectReason> rejectReason() {
        return Optional.ofNullable(this.rejectReason);
    }

    public @NotNull ArenaAllocationRejectReason requireRejectReason() {
        if (this.rejectReason == null) {
            throw new IllegalStateException("Arena allocation was accepted");
        }
        return this.rejectReason;
    }
}
