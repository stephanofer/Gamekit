package network.hera.gamekit.arena;

import java.util.Objects;
import java.util.Set;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;

public record ArenaRequirements(
        @NotNull GameId gameId,
        @NotNull VariantId variantId,
        @NotNull Set<String> requiredTags,
        int requiredPlayers
) {

    public ArenaRequirements {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(variantId, "variantId");
        requiredTags = ArenaDefinition.copyTags(requiredTags);
        if (requiredPlayers <= 0) {
            throw new IllegalArgumentException("Arena requiredPlayers must be greater than zero.");
        }
    }
}
