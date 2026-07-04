package network.hera.gamekit.queue.ticket;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record QueueTicketId(@NotNull UUID value) {

    public QueueTicketId {
        Objects.requireNonNull(value, "value");
    }

    public static @NotNull QueueTicketId random() {
        return new QueueTicketId(UUID.randomUUID());
    }

    public static @NotNull QueueTicketId of(@NotNull UUID value) {
        return new QueueTicketId(value);
    }

    @Override
    public @NotNull String toString() {
        return this.value.toString();
    }
}
