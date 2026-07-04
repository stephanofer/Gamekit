package network.hera.gamekit.network.routing;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import network.hera.gamekit.network.admission.AdmissionType;
import network.hera.gamekit.network.admission.WaitingRoomId;
import network.hera.gamekit.network.registry.ServerRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RoutingRequest(
        @NotNull PlayerId playerId,
        @NotNull AdmissionType admissionType,
        @NotNull GameId gameId,
        @Nullable VariantId variantId,
        @NotNull ServerRole targetRole,
        @Nullable ServerId preferredServerId,
        @Nullable ArenaId arenaId,
        @Nullable WaitingRoomId waitingRoomId,
        @Nullable MatchId matchId,
        @NotNull Instant requestedAt,
        @NotNull Duration admissionTtl
) {

    public RoutingRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(admissionType, "admissionType");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(targetRole, "targetRole");
        Objects.requireNonNull(requestedAt, "requestedAt");
        Objects.requireNonNull(admissionTtl, "admissionTtl");
        if (admissionTtl.isZero() || admissionTtl.isNegative()) {
            throw new IllegalArgumentException("Routing admission TTL must be positive.");
        }
    }

    public @NotNull Optional<VariantId> variantIdOptional() {
        return Optional.ofNullable(this.variantId);
    }

    public @NotNull Optional<ServerId> preferredServerIdOptional() {
        return Optional.ofNullable(this.preferredServerId);
    }

    public @NotNull Optional<ArenaId> arenaIdOptional() {
        return Optional.ofNullable(this.arenaId);
    }

    public @NotNull Optional<WaitingRoomId> waitingRoomIdOptional() {
        return Optional.ofNullable(this.waitingRoomId);
    }

    public @NotNull Optional<MatchId> matchIdOptional() {
        return Optional.ofNullable(this.matchId);
    }
}
