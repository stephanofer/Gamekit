package network.hera.gamekit.core.id;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record MatchId(@NotNull UUID value) {

    public MatchId {
        Objects.requireNonNull(value, "value");
    }

    public static @NotNull MatchId of(@NotNull UUID value) {
        return new MatchId(value);
    }

    public static @NotNull MatchId random() {
        return new MatchId(UUID.randomUUID());
    }

    @Override
    public @NotNull String toString() {
        return this.value.toString();
    }
}
