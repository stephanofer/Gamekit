package network.hera.gamekit.network.admission;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public record WaitingRoomId(@NotNull String value) {

    private static final Pattern FORMAT = Pattern.compile("[a-z0-9]+(?:_[a-z0-9]+)*");

    public WaitingRoomId {
        Objects.requireNonNull(value, "value");
        if (value.isEmpty() || value.length() > 64 || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("WaitingRoomId must use lowercase_snake_case and be at most 64 characters.");
        }
    }

    public static @NotNull WaitingRoomId of(@NotNull String value) {
        return new WaitingRoomId(value);
    }

    public static @NotNull WaitingRoomId fromMatchScoped(@NotNull String value) {
        return of(Objects.requireNonNull(value, "value"));
    }

    @Override
    public @NotNull String toString() {
        return this.value;
    }
}
