package network.hera.gamekit.arena;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public record ArenaSlot(
        @NotNull ArenaId arenaId,
        @NotNull String templateId,
        @NotNull GameId gameId,
        @NotNull ServerId serverId,
        @NotNull String worldName,
        @NotNull ArenaState state,
        @NotNull Set<String> tags,
        int maxPlayers,
        @NotNull Instant updatedAt,
        @NotNull Duration ttl
) {

    public ArenaSlot {
        Objects.requireNonNull(arenaId, "arenaId");
        templateId = ArenaDefinition.requireText("templateId", templateId);
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(serverId, "serverId");
        worldName = ArenaDefinition.requireText("worldName", worldName);
        Objects.requireNonNull(state, "state");
        tags = ArenaDefinition.copyTags(tags);
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Arena maxPlayers must be greater than zero.");
        }
        Objects.requireNonNull(updatedAt, "updatedAt");
        Objects.requireNonNull(ttl, "ttl");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Arena slot TTL must be positive.");
        }
    }

    public boolean expiredAt(@NotNull Instant now) {
        return !this.updatedAt.plus(this.ttl).isAfter(Objects.requireNonNull(now, "now"));
    }

    public boolean availableAt(@NotNull Instant now) {
        return this.state.reservable() && !expiredAt(now);
    }

    public boolean matches(@NotNull ArenaRequirements requirements, @NotNull Instant now) {
        Objects.requireNonNull(requirements, "requirements");
        return availableAt(now)
            && this.gameId.equals(requirements.gameId())
            && this.maxPlayers >= requirements.requiredPlayers()
            && this.tags.containsAll(requirements.requiredTags());
    }

    public @NotNull ArenaSlot withState(@NotNull ArenaState state, @NotNull Instant updatedAt) {
        return new ArenaSlot(
            this.arenaId,
            this.templateId,
            this.gameId,
            this.serverId,
            this.worldName,
            state,
            this.tags,
            this.maxPlayers,
            updatedAt,
            this.ttl
        );
    }
}
