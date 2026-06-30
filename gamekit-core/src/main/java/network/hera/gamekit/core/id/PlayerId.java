package network.hera.gamekit.core.id;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record PlayerId(@NotNull UUID value) {

    public PlayerId {
        Objects.requireNonNull(value, "value");
    }

    public static @NotNull PlayerId of(@NotNull UUID value) {
        return new PlayerId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value.toString();
    }
}
