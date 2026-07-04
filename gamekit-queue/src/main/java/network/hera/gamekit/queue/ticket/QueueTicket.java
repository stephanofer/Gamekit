package network.hera.gamekit.queue.ticket;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import org.jetbrains.annotations.NotNull;

public record QueueTicket(
        @NotNull QueueTicketId ticketId,
        @NotNull QueueId queueId,
        @NotNull PlayerId playerId,
        @NotNull Instant createdAt
) {

    public QueueTicket {
        Objects.requireNonNull(ticketId, "ticketId");
        Objects.requireNonNull(queueId, "queueId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static @NotNull QueueTicket solo(@NotNull QueueId queueId, @NotNull PlayerId playerId, @NotNull Instant createdAt) {
        return new QueueTicket(QueueTicketId.random(), queueId, playerId, createdAt);
    }
}
