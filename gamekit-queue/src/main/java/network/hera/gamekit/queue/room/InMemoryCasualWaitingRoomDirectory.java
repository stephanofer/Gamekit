package network.hera.gamekit.queue.room;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.network.admission.WaitingRoomId;
import network.hera.gamekit.queue.definition.QueueDefinition;
import org.jetbrains.annotations.NotNull;

public final class InMemoryCasualWaitingRoomDirectory implements CasualWaitingRoomDirectory {

    private final Map<WaitingRoomId, CasualWaitingRoomView> rooms = new ConcurrentHashMap<>();

    public void record(@NotNull CasualWaitingRoomView room) {
        Objects.requireNonNull(room, "room");
        this.rooms.put(room.waitingRoomId(), room);
    }

    public void remove(@NotNull WaitingRoomId waitingRoomId) {
        this.rooms.remove(Objects.requireNonNull(waitingRoomId, "waitingRoomId"));
    }

    @Override
    public @NotNull Optional<CasualWaitingRoomView> findJoinable(@NotNull QueueDefinition queue, @NotNull Instant now) {
        Objects.requireNonNull(queue, "queue");
        Objects.requireNonNull(now, "now");
        return this.rooms.values().stream()
            .filter(room -> room.joinableFor(queue.queueId(), now))
            .min(Comparator.comparingInt(CasualWaitingRoomView::currentPlayers).reversed()
                .thenComparing(room -> room.waitingRoomId().toString()));
    }
}
