package network.hera.gamekit.network.admission;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record AdmissionRequestId(@NotNull UUID value) {

    public AdmissionRequestId {
        Objects.requireNonNull(value, "value");
    }

    public static @NotNull AdmissionRequestId random() {
        return new AdmissionRequestId(UUID.randomUUID());
    }

    public static @NotNull AdmissionRequestId of(@NotNull UUID value) {
        return new AdmissionRequestId(value);
    }

    public static @NotNull AdmissionRequestId parse(@NotNull String value) {
        return new AdmissionRequestId(UUID.fromString(Objects.requireNonNull(value, "value")));
    }

    @Override
    public @NotNull String toString() {
        return this.value.toString();
    }
}
