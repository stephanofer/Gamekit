package network.hera.gamekit.core.id;

import org.jetbrains.annotations.NotNull;

public record ArenaId(@NotNull String value) {

    public ArenaId {
        value = IdFormats.requireDomainId("ArenaId", value);
    }

    public static @NotNull ArenaId of(@NotNull String value) {
        return new ArenaId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
