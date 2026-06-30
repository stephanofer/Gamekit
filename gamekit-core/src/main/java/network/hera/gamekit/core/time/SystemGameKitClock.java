package network.hera.gamekit.core.time;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public final class SystemGameKitClock implements GameKitClock {

    @Override
    public @NotNull Instant now() {
        return Instant.now();
    }
}
