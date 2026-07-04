package network.hera.gamekit.queue.casual;

import java.util.Objects;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.queue.definition.QueueDefinition;
import org.jetbrains.annotations.NotNull;

public record CasualQueueRequest(@NotNull PlayerId playerId, @NotNull QueueDefinition queue) {

    public CasualQueueRequest {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(queue, "queue");
    }
}
