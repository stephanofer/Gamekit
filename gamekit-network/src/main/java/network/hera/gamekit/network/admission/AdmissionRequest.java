package network.hera.gamekit.network.admission;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AdmissionRequest(
        @NotNull AdmissionRequestId id,
        @NotNull PlayerId playerId,
        @NotNull AdmissionType type,
        @NotNull ServerId targetServerId,
        @NotNull GameId gameId,
        @Nullable VariantId variantId,
        @Nullable ArenaId arenaId,
        @Nullable WaitingRoomId waitingRoomId,
        @Nullable MatchId matchId,
        @NotNull Instant createdAt,
        @NotNull Instant expiresAt
) {

    public AdmissionRequest {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(targetServerId, "targetServerId");
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("AdmissionRequest expiresAt must be after createdAt.");
        }
    }

    public boolean expiredAt(@NotNull Instant now) {
        return !this.expiresAt.isAfter(Objects.requireNonNull(now, "now"));
    }

    public @NotNull Optional<VariantId> variantIdOptional() {
        return Optional.ofNullable(this.variantId);
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

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private AdmissionRequestId id = AdmissionRequestId.random();
        private PlayerId playerId;
        private AdmissionType type;
        private ServerId targetServerId;
        private GameId gameId;
        private VariantId variantId;
        private ArenaId arenaId;
        private WaitingRoomId waitingRoomId;
        private MatchId matchId;
        private Instant createdAt;
        private Instant expiresAt;

        private Builder() {
        }

        public @NotNull Builder id(@NotNull AdmissionRequestId id) { this.id = id; return this; }
        public @NotNull Builder playerId(@NotNull PlayerId playerId) { this.playerId = playerId; return this; }
        public @NotNull Builder type(@NotNull AdmissionType type) { this.type = type; return this; }
        public @NotNull Builder targetServerId(@NotNull ServerId targetServerId) { this.targetServerId = targetServerId; return this; }
        public @NotNull Builder gameId(@NotNull GameId gameId) { this.gameId = gameId; return this; }
        public @NotNull Builder variantId(@Nullable VariantId variantId) { this.variantId = variantId; return this; }
        public @NotNull Builder arenaId(@Nullable ArenaId arenaId) { this.arenaId = arenaId; return this; }
        public @NotNull Builder waitingRoomId(@Nullable WaitingRoomId waitingRoomId) { this.waitingRoomId = waitingRoomId; return this; }
        public @NotNull Builder matchId(@Nullable MatchId matchId) { this.matchId = matchId; return this; }
        public @NotNull Builder createdAt(@NotNull Instant createdAt) { this.createdAt = createdAt; return this; }
        public @NotNull Builder expiresAt(@NotNull Instant expiresAt) { this.expiresAt = expiresAt; return this; }

        public @NotNull AdmissionRequest build() {
            return new AdmissionRequest(
                this.id,
                this.playerId,
                this.type,
                this.targetServerId,
                this.gameId,
                this.variantId,
                this.arenaId,
                this.waitingRoomId,
                this.matchId,
                this.createdAt,
                this.expiresAt
            );
        }
    }
}
