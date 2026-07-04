package network.hera.gamekit.queue.definition;

import java.time.Duration;
import java.util.Objects;
import network.hera.gamekit.arena.ArenaRequirements;
import network.hera.gamekit.core.definition.MatchVariant;
import network.hera.gamekit.core.id.QueueId;
import org.jetbrains.annotations.NotNull;

public record QueueDefinition(
        @NotNull QueueId queueId,
        @NotNull MatchVariant variant,
        @NotNull ArenaRequirements arenaRequirements,
        int minPlayers,
        @NotNull Duration admissionTtl,
        @NotNull Duration arenaReservationTtl
) {

    public QueueDefinition {
        Objects.requireNonNull(queueId, "queueId");
        Objects.requireNonNull(variant, "variant");
        Objects.requireNonNull(arenaRequirements, "arenaRequirements");
        Objects.requireNonNull(admissionTtl, "admissionTtl");
        Objects.requireNonNull(arenaReservationTtl, "arenaReservationTtl");
        if (!queueId.equals(variant.queueId())) {
            throw new IllegalArgumentException("QueueDefinition queueId must match variant queueId.");
        }
        if (!queueId.gameId().equals(arenaRequirements.gameId()) || !queueId.variantId().equals(arenaRequirements.variantId())) {
            throw new IllegalArgumentException("QueueDefinition arena requirements must match queue id.");
        }
        if (minPlayers <= 0) {
            throw new IllegalArgumentException("QueueDefinition minPlayers must be greater than zero.");
        }
        if (minPlayers > variant.teamSpec().maxPlayers()) {
            throw new IllegalArgumentException("QueueDefinition minPlayers cannot exceed variant max players.");
        }
        if (arenaRequirements.requiredPlayers() < minPlayers) {
            throw new IllegalArgumentException("QueueDefinition arena required players cannot be lower than minPlayers.");
        }
        if (admissionTtl.isZero() || admissionTtl.isNegative()) {
            throw new IllegalArgumentException("QueueDefinition admission TTL must be positive.");
        }
        if (arenaReservationTtl.isZero() || arenaReservationTtl.isNegative()) {
            throw new IllegalArgumentException("QueueDefinition arena reservation TTL must be positive.");
        }
    }
}
