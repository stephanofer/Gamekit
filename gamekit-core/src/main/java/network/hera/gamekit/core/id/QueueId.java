package network.hera.gamekit.core.id;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record QueueId(@NotNull GameId gameId, @NotNull VariantId variantId) {

    public QueueId {
        Objects.requireNonNull(gameId, "gameId");
        Objects.requireNonNull(variantId, "variantId");
    }

    public static @NotNull QueueId of(@NotNull GameId gameId, @NotNull VariantId variantId) {
        return new QueueId(gameId, variantId);
    }

    @Override
    public @NotNull String toString() {
        return this.gameId + ":" + this.variantId;
    }
}
