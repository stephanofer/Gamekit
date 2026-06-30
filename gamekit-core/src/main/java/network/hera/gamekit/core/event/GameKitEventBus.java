package network.hera.gamekit.core.event;

import org.jetbrains.annotations.NotNull;

public interface GameKitEventBus {

    void publish(@NotNull GameKitEvent event);
}
