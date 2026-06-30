package network.hera.gamekit.testkit.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.time.GameKitClock;
import org.jetbrains.annotations.NotNull;

public final class FakeGameKitClock implements GameKitClock {

    private Instant currentTime;

    public FakeGameKitClock(@NotNull Instant currentTime) {
        this.currentTime = Objects.requireNonNull(currentTime, "currentTime");
    }

    public static @NotNull FakeGameKitClock fixedAt(@NotNull Instant currentTime) {
        return new FakeGameKitClock(currentTime);
    }

    @Override
    public @NotNull Instant now() {
        return this.currentTime;
    }

    public void advance(@NotNull Duration duration) {
        this.currentTime = this.currentTime.plus(Objects.requireNonNull(duration, "duration"));
    }
}
