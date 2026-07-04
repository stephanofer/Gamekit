package network.hera.gamekit.queue.ticket;

import java.util.List;
import java.util.Optional;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import org.jetbrains.annotations.NotNull;

public interface QueueTicketStore {

    @NotNull Optional<QueueTicket> findByPlayer(@NotNull PlayerId playerId);

    boolean putIfAbsent(@NotNull QueueTicket ticket);

    void remove(@NotNull PlayerId playerId);

    @NotNull List<QueueTicket> findByQueue(@NotNull QueueId queueId);
}
