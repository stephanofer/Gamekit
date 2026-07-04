package network.hera.gamekit.queue.ticket;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.QueueId;
import org.jetbrains.annotations.NotNull;

public final class InMemoryQueueTicketStore implements QueueTicketStore {

    private final ConcurrentMap<PlayerId, QueueTicket> tickets = new ConcurrentHashMap<>();

    @Override
    public @NotNull Optional<QueueTicket> findByPlayer(@NotNull PlayerId playerId) {
        return Optional.ofNullable(this.tickets.get(Objects.requireNonNull(playerId, "playerId")));
    }

    @Override
    public boolean putIfAbsent(@NotNull QueueTicket ticket) {
        Objects.requireNonNull(ticket, "ticket");
        return this.tickets.putIfAbsent(ticket.playerId(), ticket) == null;
    }

    @Override
    public void remove(@NotNull PlayerId playerId) {
        this.tickets.remove(Objects.requireNonNull(playerId, "playerId"));
    }

    @Override
    public @NotNull List<QueueTicket> findByQueue(@NotNull QueueId queueId) {
        Objects.requireNonNull(queueId, "queueId");
        return this.tickets.values().stream()
            .filter(ticket -> ticket.queueId().equals(queueId))
            .sorted(Comparator.comparing(QueueTicket::createdAt))
            .toList();
    }
}
