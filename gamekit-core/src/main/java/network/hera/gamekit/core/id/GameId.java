package network.hera.gamekit.core.id;

import org.jetbrains.annotations.NotNull;

public record GameId(@NotNull String value) {

    public GameId {
        value = IdFormats.requireDomainId("GameId", value);
    }

    public static @NotNull GameId of(@NotNull String value) {
        return new GameId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
