package network.hera.gamekit.session.activeplay;

import java.util.Objects;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.result.Decision;
import org.jetbrains.annotations.NotNull;

public final class ActivePlayResolver {

    private final ActivePlayStore activePlayStore;

    public ActivePlayResolver(@NotNull ActivePlayStore activePlayStore) {
        this.activePlayStore = Objects.requireNonNull(activePlayStore, "activePlayStore");
    }

    public @NotNull Decision<ActivePlayRejectReason> resolve(@NotNull PlayerId playerId) {
        return this.activePlayStore.findByPlayer(playerId)
                .map(ActivePlayResolver::rejectReasonFor)
                .map(Decision::<ActivePlayRejectReason>reject)
                .orElseGet(Decision::accept);
    }

    private static @NotNull ActivePlayRejectReason rejectReasonFor(@NotNull ActivePlaySession session) {
        return switch (session.type()) {
            case QUEUE -> ActivePlayRejectReason.ALREADY_IN_QUEUE;
            case WAITING_ROOM -> ActivePlayRejectReason.ALREADY_IN_WAITING_ROOM;
            case MATCH_STARTING, MATCH_RUNNING -> ActivePlayRejectReason.ALREADY_IN_MATCH;
            case SPECTATING -> ActivePlayRejectReason.ALREADY_SPECTATING;
            case RECONNECTABLE_MATCH -> ActivePlayRejectReason.RECONNECTABLE_MATCH_EXISTS;
        };
    }
}
