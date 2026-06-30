package network.hera.gamekit.core.definition;

import java.util.Objects;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.QueueId;
import network.hera.gamekit.core.id.VariantId;
import org.jetbrains.annotations.NotNull;

public record MatchVariant(
        @NotNull GameId gameId,
        @NotNull VariantId variantId,
        @NotNull MatchKind kind,
        @NotNull TeamSpec teamSpec
) {

    public MatchVariant {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(teamSpec, "teamSpec");
    }

    public static @NotNull MatchVariant of(
            @NotNull GameId gameId,
            @NotNull VariantId variantId,
            @NotNull MatchKind kind,
            @NotNull TeamSpec teamSpec
    ) {
        return new MatchVariant(gameId, variantId, kind, teamSpec);
    }

    public @NotNull QueueId queueId() {
        return QueueId.of(this.gameId, this.variantId);
    }
}
