package network.hera.gamekit.testkit.network;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import network.hera.gamekit.core.id.MatchId;
import network.hera.gamekit.network.location.MatchLocation;
import network.hera.gamekit.network.location.MatchLocationRegistry;
import org.jetbrains.annotations.NotNull;

public final class FakeMatchLocationRegistry implements MatchLocationRegistry {

    private final Map<MatchId, MatchLocation> locations = new ConcurrentHashMap<>();

    @Override
    public @NotNull CompletableFuture<Void> save(@NotNull MatchLocation location) {
        this.locations.put(location.matchId(), location);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Optional<MatchLocation>> find(@NotNull MatchId matchId) {
        return CompletableFuture.completedFuture(Optional.ofNullable(this.locations.get(matchId)));
    }

    @Override
    public @NotNull CompletableFuture<Void> remove(@NotNull MatchId matchId) {
        this.locations.remove(matchId);
        return CompletableFuture.completedFuture(null);
    }
}
