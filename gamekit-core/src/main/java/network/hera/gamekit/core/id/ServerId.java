package network.hera.gamekit.core.id;

import org.jetbrains.annotations.NotNull;

public record ServerId(@NotNull String value) {

    public ServerId {
        value = IdFormats.requireServerId(value);
    }

    public static @NotNull ServerId of(@NotNull String value) {
        return new ServerId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
