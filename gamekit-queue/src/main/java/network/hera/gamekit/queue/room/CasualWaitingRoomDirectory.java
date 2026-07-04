package network.hera.gamekit.queue.room;

import java.time.Instant;
import java.util.Optional;
import network.hera.gamekit.queue.definition.QueueDefinition;
import org.jetbrains.annotations.NotNull;

public interface CasualWaitingRoomDirectory {

    @NotNull Optional<CasualWaitingRoomView> findJoinable(@NotNull QueueDefinition queue, @NotNull Instant now);
}
