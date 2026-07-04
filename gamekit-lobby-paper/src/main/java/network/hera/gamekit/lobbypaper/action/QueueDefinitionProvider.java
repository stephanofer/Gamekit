package network.hera.gamekit.lobbypaper.action;

import java.util.Optional;
import network.hera.gamekit.core.id.QueueId;
import network.hera.gamekit.queue.definition.QueueDefinition;
import org.jetbrains.annotations.NotNull;

public interface QueueDefinitionProvider {

    @NotNull Optional<QueueDefinition> findQueue(@NotNull QueueId queueId);
}
