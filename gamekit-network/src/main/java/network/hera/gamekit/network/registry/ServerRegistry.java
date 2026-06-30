package network.hera.gamekit.network.registry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.id.GameId;
import network.hera.gamekit.core.id.ServerId;
import org.jetbrains.annotations.NotNull;

public interface ServerRegistry {

    @NotNull CompletableFuture<Void> recordHeartbeat(@NotNull ServerHeartbeat heartbeat);

    @NotNull CompletableFuture<Optional<RegisteredServer>> find(@NotNull ServerId serverId);

    @NotNull CompletableFuture<List<RegisteredServer>> findAvailable(@NotNull GameId gameId, @NotNull ServerRole role);

    @NotNull CompletableFuture<Void> markOffline(@NotNull ServerId serverId);
}
