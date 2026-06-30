package network.hera.gamekit.network.routing;

import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.AdmissionRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RoutingDecision {

    private final @Nullable ServerId serverId;
    private final @Nullable AdmissionRequest admissionRequest;
    private final @Nullable RoutingRejectReason rejectReason;

    private RoutingDecision(@Nullable ServerId serverId, @Nullable AdmissionRequest admissionRequest, @Nullable RoutingRejectReason rejectReason) {
        this.serverId = serverId;
        this.admissionRequest = admissionRequest;
        this.rejectReason = rejectReason;
    }

    public static @NotNull RoutingDecision accept(@NotNull ServerId serverId, @NotNull AdmissionRequest admissionRequest) {
        return new RoutingDecision(Objects.requireNonNull(serverId, "serverId"), Objects.requireNonNull(admissionRequest, "admissionRequest"), null);
    }

    public static @NotNull RoutingDecision reject(@NotNull RoutingRejectReason reason) {
        return new RoutingDecision(null, null, Objects.requireNonNull(reason, "reason"));
    }

    public boolean accepted() {
        return this.rejectReason == null;
    }

    public boolean rejected() {
        return this.rejectReason != null;
    }

    public @NotNull Optional<ServerId> serverId() {
        return Optional.ofNullable(this.serverId);
    }

    public @NotNull ServerId requireServerId() {
        if (this.serverId == null) {
            throw new IllegalStateException("Routing decision was rejected.");
        }
        return this.serverId;
    }

    public @NotNull Optional<AdmissionRequest> admissionRequest() {
        return Optional.ofNullable(this.admissionRequest);
    }

    public @NotNull AdmissionRequest requireAdmissionRequest() {
        if (this.admissionRequest == null) {
            throw new IllegalStateException("Routing decision was rejected.");
        }
        return this.admissionRequest;
    }

    public @NotNull Optional<RoutingRejectReason> rejectReason() {
        return Optional.ofNullable(this.rejectReason);
    }
}
