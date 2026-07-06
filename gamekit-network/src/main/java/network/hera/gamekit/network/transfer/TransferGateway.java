package network.hera.gamekit.network.transfer;

import java.util.concurrent.CompletableFuture;
import network.hera.gamekit.core.result.Decision;
import org.jetbrains.annotations.NotNull;

public interface TransferGateway {

    @NotNull CompletableFuture<Decision<TransferRejectReason>> transfer(@NotNull TransferRequest request);
}
