package network.hera.gamekit.core.definition;

import java.util.Objects;
import network.hera.gamekit.core.id.GameId;
import org.jetbrains.annotations.NotNull;

public record GameDefinition(@NotNull GameId gameId) {

    public GameDefinition {
        Objects.requireNonNull(gameId, "gameId");
    }

    public static @NotNull GameDefinition of(@NotNull GameId gameId) {
        return new GameDefinition(gameId);
    }
}
