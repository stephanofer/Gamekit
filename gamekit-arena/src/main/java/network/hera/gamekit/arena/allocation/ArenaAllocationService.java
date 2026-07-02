package network.hera.gamekit.arena.allocation;

import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public interface ArenaAllocationService {

    @NotNull CompletableFuture<ArenaAllocationResult> tryReserve(@NotNull ArenaAllocationRequest request);
}
