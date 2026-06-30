package network.hera.gamekit.session.activeplay;

import java.util.Optional;
import network.hera.gamekit.core.id.PlayerId;
import org.jetbrains.annotations.NotNull;

public interface ActivePlayStore {

    @NotNull Optional<ActivePlaySession> findByPlayer(@NotNull PlayerId playerId);

    boolean putIfAbsent(@NotNull ActivePlaySession session);

    void remove(@NotNull PlayerId playerId);
}
