package network.hera.gamekit.network.routing;

import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public interface RoutingService {

    @NotNull CompletableFuture<RoutingDecision> route(@NotNull RoutingRequest request);
}
