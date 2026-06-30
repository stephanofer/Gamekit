package network.hera.gamekit.core.result;

import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Decision<R> {

    private final @Nullable R rejectReason;

    private Decision(@Nullable R rejectReason) {
        this.rejectReason = rejectReason;
    }

    public static <R> @NotNull Decision<R> accept() {
        return new Decision<>(null);
    }

    public static <R> @NotNull Decision<R> reject(@NotNull R reason) {
        return new Decision<>(Objects.requireNonNull(reason, "reason"));
    }

    public boolean accepted() {
        return this.rejectReason == null;
    }

    public boolean rejected() {
        return this.rejectReason != null;
    }

    public @NotNull Optional<R> rejectReason() {
        return Optional.ofNullable(this.rejectReason);
    }

    public @NotNull R requireRejectReason() {
        if (this.rejectReason == null) {
            throw new IllegalStateException("Decision was accepted");
        }
        return this.rejectReason;
    }
}
