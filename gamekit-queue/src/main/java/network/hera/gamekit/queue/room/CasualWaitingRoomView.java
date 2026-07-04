package network.hera.gamekit.queue.room;

import java.time.Instant;
import java.util.Objects;
import network.hera.gamekit.core.id.ArenaId;
import network.hera.gamekit.core.id.QueueId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.admission.WaitingRoomId;
import org.jetbrains.annotations.NotNull;

public record CasualWaitingRoomView(
        @NotNull WaitingRoomId waitingRoomId,
        @NotNull ArenaId arenaId,
        @NotNull ServerId serverId,
        @NotNull QueueId queueId,
        int currentPlayers,
        int maxPlayers,
        @NotNull Instant expiresAt
) {

    public CasualWaitingRoomView {
        Objects.requireNonNull(waitingRoomId, "waitingRoomId");
        Objects.requireNonNull(arenaId, "arenaId");
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(queueId, "queueId");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (currentPlayers < 0) {
            throw new IllegalArgumentException("Waiting room currentPlayers must not be negative.");
        }
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Waiting room maxPlayers must be greater than zero.");
        }
        if (currentPlayers > maxPlayers) {
            throw new IllegalArgumentException("Waiting room currentPlayers cannot exceed maxPlayers.");
        }
    }

    public boolean joinableFor(@NotNull QueueId queueId, @NotNull Instant now) {
        Objects.requireNonNull(queueId, "queueId");
        Objects.requireNonNull(now, "now");
        return this.queueId.equals(queueId) && this.currentPlayers < this.maxPlayers && this.expiresAt.isAfter(now);
    }
}
