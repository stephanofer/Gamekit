package network.hera.gamekit.core.time;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public interface GameKitClock {

    @NotNull Instant now();
}
