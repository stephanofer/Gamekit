package network.hera.gamekit.session.activeplay;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import network.hera.gamekit.core.id.PlayerId;
import org.jetbrains.annotations.NotNull;

public final class InMemoryActivePlayStore implements ActivePlayStore {

    private final ConcurrentMap<PlayerId, ActivePlaySession> sessions = new ConcurrentHashMap<>();

    @Override
    public @NotNull Optional<ActivePlaySession> findByPlayer(@NotNull PlayerId playerId) {
        return Optional.ofNullable(this.sessions.get(Objects.requireNonNull(playerId, "playerId")));
    }

    @Override
    public boolean putIfAbsent(@NotNull ActivePlaySession session) {
        Objects.requireNonNull(session, "session");
        return this.sessions.putIfAbsent(session.playerId(), session) == null;
    }

    @Override
    public void remove(@NotNull PlayerId playerId) {
        this.sessions.remove(Objects.requireNonNull(playerId, "playerId"));
    }
}
