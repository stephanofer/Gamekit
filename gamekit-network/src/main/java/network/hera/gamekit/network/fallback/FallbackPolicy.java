package network.hera.gamekit.network.fallback;

import java.util.Optional;
import network.hera.gamekit.core.id.PlayerId;
import network.hera.gamekit.core.id.ServerId;
import network.hera.gamekit.network.transfer.TransferRejectReason;
import org.jetbrains.annotations.NotNull;

public interface FallbackPolicy {

    @NotNull Optional<ServerId> fallbackFor(@NotNull PlayerId playerId, @NotNull TransferRejectReason reason);
}
