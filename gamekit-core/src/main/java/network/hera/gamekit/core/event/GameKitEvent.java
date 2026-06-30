package network.hera.gamekit.core.event;

import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public interface GameKitEvent {

    @NotNull Instant occurredAt();
}
