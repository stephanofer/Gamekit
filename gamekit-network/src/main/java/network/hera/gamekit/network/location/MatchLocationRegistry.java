package network.hera.gamekit.network.location;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.MatchId;
import org.jetbrains.annotations.NotNull;

public interface MatchLocationRegistry {

    @NotNull CompletableFuture<Void> save(@NotNull MatchLocation location);

    @NotNull CompletableFuture<Optional<MatchLocation>> find(@NotNull MatchId matchId);

    @NotNull CompletableFuture<Void> remove(@NotNull MatchId matchId);
}
